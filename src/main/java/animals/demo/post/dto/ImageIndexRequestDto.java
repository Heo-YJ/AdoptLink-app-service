package animals.demo.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ImageIndexRequestDto {
    private String postImageUrl;
    private Integer orderIndex;
}
