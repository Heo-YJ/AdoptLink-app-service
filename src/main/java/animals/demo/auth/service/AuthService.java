package animals.demo.auth.service;

import animals.demo.auth.dto.*;
import animals.demo.auth.entity.RefreshToken;
import animals.demo.auth.repository.RefreshTokenRepository;
import animals.demo.common.CustomException;
import animals.demo.common.ErrorCode;
import animals.demo.common.sms.SmsService;
import animals.demo.notification.entity.Channel;
import animals.demo.notification.entity.NotificationPreference;
import animals.demo.notification.entity.Type;
import animals.demo.notification.repository.NotificationRepository;
import animals.demo.security.JwtTokenProvider;
import animals.demo.user.entity.User;
import animals.demo.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
@RequiredArgsConstructor //생성자 직접 안써도 됨
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final SmsService smsService;
    private final NotificationRepository notificationRepository;

    //회원가입
    @Transactional
    public SignupResponseDto signup(SignupRequestDto signupRequestDto) {


        //인증된 휴대폰인지 확인
        String verifiedPhone = redisTemplate.opsForValue().get("verified: " + signupRequestDto.getVerificationId());
            if(verifiedPhone == null) {
                throw  new CustomException(ErrorCode.PHONE_NOT_VERIFIED);
        }

        //아이디 중복확인
        if(userRepository.findByLoginId(signupRequestDto.getLoginId()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        //닉네임 중복확인
        if(userRepository.findByNickname(signupRequestDto.getNickname()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        //비밀번호 확인
        if (!signupRequestDto.getPassword().equals(signupRequestDto.getPasswordConfirm())) {
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }

        //비밀번호 암호화
        //getPassword()로 꺼내서 암호화한 다음 toEntity(encodedPassword)에 넣음
        String encodedPassword = passwordEncoder.encode(signupRequestDto.getPassword());
        User user = userRepository.save(signupRequestDto.toEntity(encodedPassword, verifiedPhone));

        redisTemplate.delete("verified: " + signupRequestDto.getVerificationId());


        // 기본 알림 설정 생성 (모두 true로)
        List<NotificationPreference> defaultNotifications = List.of(
                NotificationPreference.builder()
                        .user(user)
                        .type(Type.CHAT_MESSAGE)
                        .channel(Channel.PUSH)
                        .enabled(true)
                        .build(),
                NotificationPreference.builder()
                        .user(user)
                        .type(Type.INQUIRY_REPLY)
                        .channel(Channel.PUSH)
                        .enabled(true)
                        .build(),
                NotificationPreference.builder()
                        .user(user)
                        .type(Type.MARKETING)
                        .channel(Channel.PUSH)
                        .enabled(true)
                        .build()
        );
        notificationRepository.saveAll(defaultNotifications);

        return SignupResponseDto.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .build();
    }

    //로그인
    @Transactional
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {

        //필수값 누락 확인
        if(loginRequestDto.getLoginId() == null || loginRequestDto.getLoginId().isEmpty()
                || loginRequestDto.getPassword() == null || loginRequestDto.getPassword().isEmpty()) {
            throw new CustomException(ErrorCode.MISSING_REQUIRED_VALUE);
        }

        //아이디 일치 확인
        User user = userRepository.findByLoginId(loginRequestDto.getLoginId())
                .orElseThrow(() -> new CustomException(ErrorCode.ID_PASSWORD_MISMATCH));

        //비밀번호 일치 확인
        if(!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPasswordHash())) {
            throw new CustomException(ErrorCode.ID_PASSWORD_MISMATCH);
        }

        //accessToken, refreshToken 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());

        //RefreshToken 저장
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .userId(user.getUserId())
                .refreshToken(refreshToken)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

    }

    //토큰 재발급
    @Transactional
    public ReissueResponseDto reissue(String refreshToken) {
        // 1. Refresh Token 검증
        if(!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Refresh Token이 유효하지 않습니다.");
        }

        // 2. 토큰에서 userId 가져오기
        Long userId = jwtTokenProvider.getUserId(refreshToken);

        // 3. DB에서 저장된 Refresh Token 가져오기
        RefreshToken savedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

        // 4. 토큰 일치 여부 확인
        if(!savedToken.getRefreshToken().equals(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 5. 유저 정보 가져오기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 6. 새 Access Token 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(userId, user.getRole().name());

        // 7. Refresh Token 업데이트
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);
        refreshTokenRepository.save(RefreshToken.builder()
                .userId(userId)
                .refreshToken(newRefreshToken)
                .build());

        return ReissueResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    //로그아웃
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    //비밀번호 변경
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequestDto changePasswordRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 현재 비밀번호 일치 확인
        if(!passwordEncoder.matches(changePasswordRequestDto.getCurrentPassword(), user.getPasswordHash())) {
            throw new CustomException(ErrorCode.CURRENT_PASSWORD_MISMATCH);
        }

        // 새 비밀번호 확인 절차
        if(!changePasswordRequestDto.getNewPassword().equals(changePasswordRequestDto.getPasswordConfirm())) {
            throw new CustomException(ErrorCode.NEW_PASSWORD_NOT_CONFIRM);
        }

        // 새 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(changePasswordRequestDto.getNewPassword());
        user.changPasswordHash(encodedPassword);
    }

    //인증번호 전송
    @Transactional
    public void sendCode(SendCodeRequestDto sendCodeRequestDto) {

        String phone = sendCodeRequestDto.getPhone();

        if (phone == null || phone.isEmpty()) {
            throw new CustomException(ErrorCode.REQUIRED_PHONE_NUMBER);
        }

        if (!phone.matches("^01[0-9]{8,9}$")) {
            throw new CustomException(ErrorCode.INVALID_PHONE_FORMAT);
        }

        if(userRepository.findByPhone(phone).isPresent()) {
            throw new CustomException(ErrorCode.EXIST_PHONE);
        }

        //인증번호 생성 (6자리)
        String code = String.valueOf((int)(Math.random() * 900000) + 100000);

        //Redis 저장 (TTL: 5분)
        redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);

        smsService.sendSms(phone, code);


    }

    //인증번호 확인
    @Transactional
    public CodeVerificationResponseDto verifyCode(CodeVerificationRequestDto codeVerificationRequestDto) {

        String phone = codeVerificationRequestDto.getPhone();
        String verifyCode = codeVerificationRequestDto.getVerifyCode();

        //필수값 누락
        if(verifyCode == null || verifyCode.isEmpty()) {
            throw new CustomException(ErrorCode.REQUIRED_PHONE_VERIFY_CODE);
        }

        //redis에 저장된 인증번호 꺼내오기
        String savedCode = redisTemplate.opsForValue().get(phone);

        //인증번호 만료 혹은 미발송
        if (savedCode == null) {
            throw new CustomException(ErrorCode.EXPIRED_VERIFY_CODE);
        }

        //인증번호 불일치
        if(!savedCode.equals(verifyCode)) {
            throw new CustomException(ErrorCode.INVALID_VERIFY_CODE);
        }

        //인증 완료 상태 저장 - verificationId 발급
        String verificationId = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("verified: " + verificationId, phone, 10, TimeUnit.MINUTES);

        redisTemplate.delete(phone);

        return CodeVerificationResponseDto.builder()
                .verificationId(verificationId)
                .build();

    }

}
