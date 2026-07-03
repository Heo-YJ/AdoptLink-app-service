package animals.demo.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateUserInfoRequestDto {
    private String nickname;
    private String profileImageUrl;
}
