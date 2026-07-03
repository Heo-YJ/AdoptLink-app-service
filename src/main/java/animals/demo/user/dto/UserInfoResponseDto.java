package animals.demo.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponseDto {
    private Long userId;
    private String nickname;
    private String profileImageUrl;
}
