package animals.demo.notice.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NoticeListResponseDto {
    private Long noticeId;
    private String title;
    private LocalDateTime createdAt;
}
