package animals.demo.post.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostFeedResponseDto {
    private Long postId;
    private String thumbnailImageUrl;
}
