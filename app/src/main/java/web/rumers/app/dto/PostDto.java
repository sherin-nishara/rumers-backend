package web.rumers.app.dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostDto {
    private Long id;
    private Long collegeId;
    private String content;
    private Integer upvote;
    private Integer downvote;
    private LocalDateTime createdAt;
}