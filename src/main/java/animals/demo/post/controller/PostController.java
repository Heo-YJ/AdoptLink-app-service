package animals.demo.post.controller;

import animals.demo.common.ApiResponse;
import animals.demo.post.dto.*;
import animals.demo.post.service.PostService;
import animals.demo.security.SecurityUtils;
import animals.demo.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.hibernate.annotations.TargetEmbeddable;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody @Valid CreatePostRequestDto createPostRequestDto) {
        Long userId = SecurityUtils.getCurrentUserId();
        CreatePostResponseDto response = postService.createPost(userId, createPostRequestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("게시글이 정상적으로 작성되었습니다.", response));
    }

    @PatchMapping("/{postId}/status")
    public ResponseEntity<?> changeStatus(@PathVariable Long postId, @RequestBody ChangePostStatusRequestDto changePostStatusRequestDto) {
        Long userId = SecurityUtils.getCurrentUserId();
        ChangePostStatusResponseDto response = postService.changeStatus(userId, postId, changePostStatusRequestDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("게시글 분양 상태가 변경되었습니다.", response));
    }

    //게시글 수정
    @PatchMapping("/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable Long postId, @RequestBody UpdatePostRequestDto updatePostRequestDto) {
        Long userId = SecurityUtils.getCurrentUserId();
        postService.updatePost(userId, postId, updatePostRequestDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("게시글이 수정되었습니다.", null));
    }

    //게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        Long userId = SecurityUtils.getCurrentUserId();
        postService.deletePost(postId, userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("게시글이 삭제되었습니다.", null));
    }

    //게시글 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(@PathVariable Long postId) {
        PostDetailResponseDto response = postService.getPost(postId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("게시글 조회에 성공했습니다.", response));
    }

    //게시글 검색
    @GetMapping
    public ResponseEntity<?> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        PostSearchListResponseDto response = postService.searchPosts(keyword, offset, limit, sort);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("게시글 검색에 성공했습니다.", response));
    }

    //스크랩
    @PostMapping("/{postId}/scrap")
    public ResponseEntity<?> scrap(@PathVariable Long postId) {
        Long userId = SecurityUtils.getCurrentUserId();
        ScrapResponseDto response = postService.scrap(postId, userId);

        String message = response.isScraped()
                ? "게시글을 스크랩했습니다."
                : "게시글을 스크랩해제했습니다.";

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok(message, response));
    }
}
