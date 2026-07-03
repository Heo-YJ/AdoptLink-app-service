package animals.demo.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CodeVerificationRequestDto {
    private String phone;
    private String verifyCode;
}
