package animals.demo.auth.dto;

import animals.demo.user.entity.AuthProvider;
import animals.demo.user.entity.Role;
import animals.demo.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class SignupRequestDto {

    private String verificationId;
    private String loginId;
    private String password;
    private String passwordConfirm;
    private String nickname;
    private String phone;

    //DB에 삽입할 Entity 객체를 만들어 리턴해주는 메서드
    public User toEntity(String encodedPassword) {
        return User.builder()
                .loginId(loginId)
                .passwordHash(encodedPassword)
                .nickname(nickname)
                .phone(phone)
                .role(Role.USER)
                .provider(AuthProvider.LOCAL)
                .build();
    }
}
