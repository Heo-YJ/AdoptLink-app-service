package animals.demo.user.controller;

import animals.demo.common.ApiResponse;
import animals.demo.post.dto.PostFeedListResponseDto;
import animals.demo.post.dto.ScrapPostListResponseDto;
import animals.demo.post.service.PostService;
import animals.demo.security.SecurityUtils;
import animals.demo.user.dto.UpdateUserInfoRequestDto;
import animals.demo.user.dto.MyInfoResponseDto;
import animals.demo.user.dto.UserInfoResponseDto;
import animals.demo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final PostService postService;

    //회원탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<?> singOut() {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.signOut(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("정상적으로 탈퇴되었습니다.", null));
    }

    //내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo() {
        Long userId = SecurityUtils.getCurrentUserId();
        MyInfoResponseDto response = userService.getMyInfo(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("내 정보 조회에 성공했습니다.", response));
    }

    //프로필 수정
    @PatchMapping("/me")
    public ResponseEntity<?> updateUserInfo(@RequestBody UpdateUserInfoRequestDto updateUserInfoRequestDto) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.updateUserInfo(userId, updateUserInfoRequestDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("프로필이 변경되었습니다.", null));
    }

    //특정 유저 정보 조회
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserInfo(@PathVariable Long userId) {
        UserInfoResponseDto response = userService.getUserInfo(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("유저 정보 조회에 성공했습니다.", response));
    }

    //내 게시글 피드 조회
    @GetMapping("/me/posts")
    public ResponseEntity<?> getMyPosts() {
        Long userId = SecurityUtils.getCurrentUserId();
        PostFeedListResponseDto response = postService.getPostFeed(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("내 게시글 피드 조회에 성공했습니다.", response));
    }

    //특정 유저 게시글 피드 조회
    @GetMapping("/{userId}/posts")
    public ResponseEntity<?> getUserPosts(@PathVariable Long userId) {
        PostFeedListResponseDto response = postService.getPostFeed(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("게시글 피드 조회에 성공했습니다.", response));
    }

    //스크랩한 게시글 조회
    @GetMapping("/me/scraps")
    public ResponseEntity<?> getScrapPost() {
        Long userId = SecurityUtils.getCurrentUserId();
        ScrapPostListResponseDto response = postService.getScrapPost(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("스크랩한 게시글 조회에 성공했습니다.", response));
    }
}
