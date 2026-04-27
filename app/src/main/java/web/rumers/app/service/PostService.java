package web.rumers.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import web.rumers.app.dto.PostDto;
import web.rumers.app.entity.College;
import web.rumers.app.entity.Post;
import web.rumers.app.repository.AppRepository;
import web.rumers.app.repository.PostRepository;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final AppRepository appRepository;
    private final ModerationService moderationService;
    private final RateLimitService rateLimitService;

    public Page<PostDto> getPostsByCollege(Long collegeId, int page) {
        Pageable pageable = PageRequest.of(page, 20,
                Sort.by("createdAt").descending());

        return postRepository
                .findByCollegeIdOrderByCreatedAtDesc(collegeId, pageable)
                .map(p -> {
                    PostDto dto = new PostDto();
                    dto.setId(p.getId());
                    dto.setCollegeId(p.getCollege().getId());
                    dto.setContent(p.getContent());
                    dto.setUpvote(p.getUpvote());
                    dto.setDownvote(p.getDownvote());
                    dto.setCreatedAt(p.getCreatedAt());
                    return dto;
                });
    }

    // Create post
    public ResponseEntity<?> createPost(PostDto dto, String clientIp) {

        // Check rate limit
        if (rateLimitService.isLimited(clientIp)) {
            return ResponseEntity.status(429).body(Map.of(
                    "success", false,
                    "rateLimited", true,
                    "message", "Too many posts! Try again in 45 mins",
                    "retryAfter", rateLimitService.getRetryAfter(clientIp)
            ));
        }

        // Get college
        College college = appRepository.findById(dto.getCollegeId())
                .orElseThrow(() -> new RuntimeException("College not found"));

        boolean safe = moderationService.moderate(dto.getContent());
        if (!safe) {
            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", "Your post violated Rumers guidelines!"
            ));
        }

        // Save only if safe
        Post post = new Post();
        post.setCollege(college);
        post.setContent(dto.getContent());
        post.setUpvote(0);
        post.setDownvote(0);
        Post saved = postRepository.save(post);

        // Track rate limit
        rateLimitService.track(clientIp);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "postId", saved.getId(),
                "message", "Posted anonymously!"
        ));
    }

    // Upvote
    public void upvote(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setUpvote(post.getUpvote() + 1);
        postRepository.save(post);
    }

    // Downvote
    public void downvote(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setDownvote(post.getDownvote() + 1);
        postRepository.save(post);
    }
}
