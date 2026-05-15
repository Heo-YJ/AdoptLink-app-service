package animals.demo.user.service;

import animals.demo.common.CustomException;
import animals.demo.common.ErrorCode;
import animals.demo.post.repository.PostImageRepository;
import animals.demo.post.repository.PostRepository;
import animals.demo.user.dto.UpdateUserInfoRequestDto;
import animals.demo.user.dto.MyInfoResponseDto;
import animals.demo.user.dto.UserInfoResponseDto;
import animals.demo.user.entity.User;
import animals.demo.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;

    //회원 탈퇴
    @Transactional
    public void signOut(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.softDelete();
    }

    //내 정보 조회
    @Transactional
    public MyInfoResponseDto getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return MyInfoResponseDto.builder()
                .loginId(user.getLoginId())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }

    //프로필 수정
    @Transactional
    public void updateUserInfo(Long userId, UpdateUserInfoRequestDto updateUserInfoRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(userRepository.findByNickname(updateUserInfoRequestDto.getNickname()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
        user.updateNickname(updateUserInfoRequestDto.getNickname());

        user.updateProfileImageUrl(updateUserInfoRequestDto.getProfileImageUrl());
    }

    //특정 사용자 조회
    @Transactional
    public UserInfoResponseDto getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return UserInfoResponseDto.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }


}