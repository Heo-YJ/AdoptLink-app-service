package animals.demo.post.service;

import animals.demo.common.CustomException;
import animals.demo.common.ErrorCode;
import animals.demo.post.entity.Post;
import animals.demo.post.repository.PostImageRepository;
import animals.demo.post.repository.PostRepository;
import animals.demo.post.dto.MyPostFeedListResponseDto;
import animals.demo.post.dto.MyPostFeedResponseDto;
import animals.demo.user.entity.User;
import animals.demo.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;

    //내 게시글 피드 조회
    @Transactional
    public MyPostFeedListResponseDto getMyPostFeed(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Post> posts = postRepository.findByAuthor_UserIdAndDeletedAtIsNull(userId);

        List<MyPostFeedResponseDto> postList = posts.stream()
                .map(post ->  {
                    String thumbnailImageUrl = postImageRepository
                            .findFirstByPost_PostIdAndOrderIndex(post.getPostId(), 1)
                            .map(postImage -> postImage.getPostImageUrl())
                            .orElse(null);
                    return MyPostFeedResponseDto.builder()
                            .postId(post.getPostId())
                            .thumbnailImageUrl(thumbnailImageUrl)
                            .build();
                })
                .toList();
        return MyPostFeedListResponseDto.builder()
                .posts(postList)
                .build();
    }
}
