package animals.demo.auth.service;

import animals.demo.auth.dto.LoginRequestDto;
import animals.demo.auth.dto.LoginResponseDto;
import animals.demo.auth.dto.SignupRequestDto;
import animals.demo.auth.dto.SignupResponseDto;
import animals.demo.common.CustomException;
import animals.demo.common.ErrorCode;
import animals.demo.security.JwtTokenProvider;
import animals.demo.user.entity.User;
import animals.demo.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor //생성자 직접 안써도 됨
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

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

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

}
