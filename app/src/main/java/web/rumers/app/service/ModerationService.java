package web.rumers.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class ModerationService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public ModerationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean moderate(String content) {
        try {
            String prompt = """
                You are a content moderator for a college anonymous app in Tamil Nadu, India.
                Students post in English and Tanglish (Tamil + English mix).
                
                Reply ONLY with JSON: {"safe": true} or {"safe": false}
                
                UNSAFE if:
                - Hate speech or slurs
                - Personal attacks or harassment
                - Sexual content
                - Threats or violence
                - Spam
                - Advertisements or promotions of any kind
                - More than one * used to hide bad words
                
                SAFE if:
                - College gossip
                - Complaints about food, wifi, professors
                - Funny observations
                - Mild frustration
                
                Post: "%s"
                """.formatted(content);

            String requestBody = """
                {
                  "contents": [{
                    "parts": [{"text": "%s"}]
                  }]
                }
                """.formatted(
                    prompt.replace("\"", "\\\"")
                            .replace("\n", "\\n")
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    apiUrl + "?key=" + apiKey,
                    entity,
                    String.class
            );

            return parseResponse(response.getBody());

        } catch (Exception e) {
            log.error("Moderation error: {}", e.getMessage());
            return true;
        }
    }

    private boolean parseResponse(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            String text = root
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text").asText();
            return mapper.readTree(text.trim())
                    .path("safe").asBoolean(true);
        } catch (Exception e) {
            log.error("Parse error: {}", e.getMessage());
            return true;
        }
    }
}