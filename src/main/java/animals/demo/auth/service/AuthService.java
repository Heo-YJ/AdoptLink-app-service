package animals.demo.auth.service;

import animals.demo.auth.dto.*;
import animals.demo.auth.entity.RefreshToken;
import animals.demo.auth.repository.RefreshTokenRepository;
import animals.demo.common.CustomException;
import animals.demo.common.ErrorCode;
import animals.demo.security.JwtTokenProvider;
import animals.demo.user.entity.User;
import animals.demo.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor //생성자 직접 안써도 됨
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;


    //회원가입
    @Transactional
    public SignupResponseDto signup(SignupRequestDto signupRequestDto) {
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
        User user = userRepository.save(signupRequestDto.toEntity(encodedPassword));

        return SignupResponseDto.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .build();
    }

    //로그인
    @Transactional
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {

        //필수값 누락 확인
        if(loginRequestDto.getLoginId().isEmpty() || loginRequestDto.getPassword().isEmpty()) {
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


}
