package animals.demo.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
//알림설정조회
public class NotificationResponseDto {
    private boolean chatNotification;
    private boolean noticeNotification;
    private boolean marketingNotification;
}
