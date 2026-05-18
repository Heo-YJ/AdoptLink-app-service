package animals.demo.post.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostImageResponseDto {
    private String postImageUrl;
    private Integer orderIndex;
}
