package web.rumers.app.controller;

import web.rumers.app.dto.PostDto;
import web.rumers.app.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import java.util.Map;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // Get posts by college
    @GetMapping("/college/{collegeId}")
    public ResponseEntity<?> getPostsByCollege(
            @PathVariable Long collegeId,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(
                postService.getPostsByCollege(collegeId, page)
        );
    }

    // Create post
    @PostMapping
    public ResponseEntity<?> createPost(
            @Valid @RequestBody PostDto dto,
            HttpServletRequest request
    ) {
        String clientIp = getClientIp(request);
        return postService.createPost(dto, clientIp);
    }

    // Upvote
    @PutMapping("/{id}/upvote")
    public ResponseEntity<?> upvote(@PathVariable Long id) {
        postService.upvote(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // Downvote
    @PutMapping("/{id}/downvote")
    public ResponseEntity<?> downvote(@PathVariable Long id) {
        postService.downvote(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // Extract real IP behind proxy
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For can have multiple IPs — take first one
        return ip.split(",")[0].trim();
    }
}