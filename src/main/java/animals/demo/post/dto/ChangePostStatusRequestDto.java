package animals.demo.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChangePostStatusRequestDto {
    private String status;
}
