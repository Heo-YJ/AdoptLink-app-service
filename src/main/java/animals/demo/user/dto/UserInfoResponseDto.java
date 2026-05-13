package animals.demo.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserInfoResponseDto {
    private String loginId;
    private String nickname;
    private String phone;
    private String role;
    private LocalDateTime createdAt;
}
