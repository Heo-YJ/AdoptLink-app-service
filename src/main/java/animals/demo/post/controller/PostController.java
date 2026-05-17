package animals.demo.post.controller;

import animals.demo.common.ApiResponse;
import animals.demo.post.dto.ChangePostStatusRequestDto;
import animals.demo.post.dto.ChangePostStatusResponseDto;
import animals.demo.post.dto.CreatePostRequestDto;
import animals.demo.post.dto.CreatePostResponseDto;
import animals.demo.post.service.PostService;
import animals.demo.security.SecurityUtils;
import animals.demo.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
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
}
