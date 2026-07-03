package animals.demo.notice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateNoticeResponseDto {
    private Long noticeId;
}
