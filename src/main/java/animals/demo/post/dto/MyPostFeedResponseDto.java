package animals.demo.post.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPostFeedResponseDto {
    private Long postId;
    private String thumbnailImageUrl;
}
