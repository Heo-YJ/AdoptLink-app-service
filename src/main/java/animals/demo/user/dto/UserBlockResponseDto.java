package animals.demo.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class UserBlockResponseDto {
    private Long userId;
}
