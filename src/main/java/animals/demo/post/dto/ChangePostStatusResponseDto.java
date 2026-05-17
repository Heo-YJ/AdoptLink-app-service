package animals.demo.post.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChangePostStatusResponseDto {
    private Long postId;
    private String status;
}
