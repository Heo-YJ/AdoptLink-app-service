package animals.demo.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CreatePostRequestDto {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    @NotNull
    private AnimalInfoRequestDto animalInfo;
    @NotNull
    private List<ImageIndexRequestDto> images;
}
