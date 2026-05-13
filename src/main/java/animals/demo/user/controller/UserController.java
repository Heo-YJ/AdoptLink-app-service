package animals.demo.user.controller;

import animals.demo.auth.dto.LoginResponseDto;
import animals.demo.common.ApiResponse;
import animals.demo.security.SecurityUtils;
import animals.demo.user.dto.UserInfoResponseDto;
import animals.demo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @DeleteMapping("/me")
    public ResponseEntity<?> singOut() {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.signOut(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("정상적으로 탈퇴되었습니다.", null));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo() {
        Long userId = SecurityUtils.getCurrentUserId();
        UserInfoResponseDto response = userService.getMyInfo(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("내 정보 조회에 성공했습니다.", response));
    }

}
