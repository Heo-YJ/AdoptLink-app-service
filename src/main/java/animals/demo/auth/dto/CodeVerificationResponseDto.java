package animals.demo.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CodeVerificationResponseDto {
    private String verificationId;
}
