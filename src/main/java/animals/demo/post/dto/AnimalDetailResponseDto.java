package animals.demo.post.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnimalDetailResponseDto {
    private String species;
    private String breed;
    private String sex;
    private Integer age;
    private Boolean neutered;
}
