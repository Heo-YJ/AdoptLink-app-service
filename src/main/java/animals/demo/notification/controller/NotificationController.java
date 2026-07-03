package animals.demo.notification.controller;

import animals.demo.common.ApiResponse;
import animals.demo.notification.dto.ChangeNotificationRequestDto;
import animals.demo.notification.dto.NotificationResponseDto;
import animals.demo.notification.service.NotificationService;
import animals.demo.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification-preferences")
public class NotificationController {

    private final NotificationService notificationService;

    //알림 설정 조회
    @GetMapping
    public ResponseEntity<?> getNotification() {
        Long userId = SecurityUtils.getCurrentUserId();
        NotificationResponseDto response = notificationService.getNotification(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("알림 설정 조회에 성공했습니다", response));
    }

    // 알림 설정 변경
    @PatchMapping
    public ResponseEntity<?> changeNotification(@RequestBody ChangeNotificationRequestDto changeNotificationRequestDto) {
        Long userId = SecurityUtils.getCurrentUserId();
        notificationService.changeNotification(userId, changeNotificationRequestDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("알림 설정이 변경되었습니다.", null));
    }
}
