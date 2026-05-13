package animals.demo.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReissueResponseDto {
    private String accessToken;
    private String refreshToken;
}
