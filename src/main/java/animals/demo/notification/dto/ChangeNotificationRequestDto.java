package animals.demo.notification.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChangeNotificationRequestDto {
    private boolean chatNotification;
    private boolean noticeNotification;
    private boolean marketingNotification;

}
