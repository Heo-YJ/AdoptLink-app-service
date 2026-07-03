package animals.demo.notification.service;

import animals.demo.common.CustomException;
import animals.demo.common.ErrorCode;
import animals.demo.notification.dto.ChangeNotificationRequestDto;
import animals.demo.notification.dto.NotificationResponseDto;
import animals.demo.notification.entity.NotificationPreference;
import animals.demo.notification.entity.Type;
import animals.demo.notification.repository.NotificationRepository;
import animals.demo.user.entity.User;
import animals.demo.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    //알림 설정 조회
    @Transactional
    public NotificationResponseDto getNotification(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<NotificationPreference> notifications =
                notificationRepository.findByUser_UserId(userId);

        boolean chatEnabled = notifications.stream()
                .filter(n -> n.getType() == Type.CHAT_MESSAGE)
                .findFirst()
                .map(NotificationPreference::isEnabled)
                .orElse(true);

        boolean noticeEnabled = notifications.stream()
                .filter(n -> n.getType() == Type.INQUIRY_REPLY)
                .findFirst()
                .map(NotificationPreference::isEnabled)
                .orElse(true);

        boolean marketingEnabled = notifications.stream()
                .filter(n -> n.getType() == Type.MARKETING)
                .findFirst()
                .map(NotificationPreference::isEnabled)
                .orElse(true);

        return NotificationResponseDto.builder()
                .chatNotification(chatEnabled)
                .noticeNotification(noticeEnabled)
                .marketingNotification(marketingEnabled)
                .build();
    }

    //알림 설정 변경
    @Transactional
    public void changeNotification(Long userId, ChangeNotificationRequestDto changeNotificationRequestDto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<NotificationPreference> notifications =
                notificationRepository.findByUser_UserId(userId);

        notifications.forEach(notification -> {
            switch (notification.getType()) {
                case CHAT_MESSAGE -> notification.updateEnabled(changeNotificationRequestDto.isChatNotification());
                case INQUIRY_REPLY -> notification.updateEnabled(changeNotificationRequestDto.isNoticeNotification());
                case MARKETING -> notification.updateEnabled(changeNotificationRequestDto.isMarketingNotification());
            }
        });

    }
}
