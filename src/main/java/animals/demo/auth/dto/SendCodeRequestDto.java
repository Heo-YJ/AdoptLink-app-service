package animals.demo.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
//인증 번호 전송
public class SendCodeRequestDto {
    private String phone;
}
