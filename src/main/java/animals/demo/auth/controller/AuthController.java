package animals.demo.auth.controller;

import animals.demo.auth.dto.*;
import animals.demo.auth.service.AuthService;
import animals.demo.common.ApiResponse;
import animals.demo.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequestDto signupRequestDto) {
        SignupResponseDto response = authService.signup(signupRequestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("회원가입이 완료되었습니다.", response));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto response = authService.login(loginRequestDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("로그인 되었습니다.", response));
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(@RequestHeader("Authorization") String authorization) {
        String refreshToken = authorization.substring(7);
        ReissueResponseDto response = authService.reissue(refreshToken);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("토큰이 재발급 되었습니다.", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        Long userId = SecurityUtils.getCurrentUserId();
        authService.logout(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("로그아웃이 완료되었습니다.", null));
    }

    @PatchMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequestDto changePasswordRequestDto) {
        Long userId = SecurityUtils.getCurrentUserId();
        authService.changePassword(userId, changePasswordRequestDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("비밀번호가 성공적으로 변경되었습니다.", null));
    }

    //인증번호 전송
    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@RequestBody SendCodeRequestDto sendCodeRequestDto) {
        authService.sendCode(sendCodeRequestDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("인증번호가 전송되었습니다.", null));
    }

    //인증번호 확인
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody CodeVerificationRequestDto codeVerificationRequestDto) {

        CodeVerificationResponseDto response = authService.verifyCode(codeVerificationRequestDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("인증되었습니다.", response));
    }
}
