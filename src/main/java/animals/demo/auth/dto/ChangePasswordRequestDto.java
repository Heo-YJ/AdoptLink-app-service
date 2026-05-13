package animals.demo.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChangePasswordRequestDto {
    private String currentPassword;
    private String newPassword;
    private String passwordConfirm;
}
