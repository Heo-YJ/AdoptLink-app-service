package animals.demo.post.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreatePostResponseDto {
    private Long postId;
    private String title;
}
