package animals.demo.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AnimalInfoRequestDto {
    private String species;
    private String breed;
    private String sex;
    private Integer age;
    private Boolean neutered;
}
