package animals.demo.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
//로그인 성공 후 클라이언트가 이후 API 요청할 때 필요한 것
public class TokenResponseDto {
    private String accessToken;
    private String refreshToken;
}
