package animals.demo.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class UpdatePostRequestDto {
    private String title;
    private String content;
    private AnimalInfoRequestDto animal;
    private List<ImageIndexRequestDto> images;
}
