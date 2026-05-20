package animals.demo.post.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScrapResponseDto {
    private Long postId;
    private boolean scraped;
}
