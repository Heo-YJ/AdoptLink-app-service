package animals.demo.post.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostSearchResponseDto {
    private Long postId;
    private String title;
    private String thumbnailImageUrl;
    private String status;
    private LocalDateTime createdAt;
}
