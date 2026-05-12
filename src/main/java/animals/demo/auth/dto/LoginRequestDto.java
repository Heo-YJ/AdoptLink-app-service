package animals.demo.auth.dto;

import animals.demo.user.entity.AuthProvider;
import animals.demo.user.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequestDto {
    private String loginId;
    private String password;
}
