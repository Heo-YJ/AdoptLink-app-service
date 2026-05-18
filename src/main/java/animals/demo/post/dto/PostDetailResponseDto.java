package animals.demo.post.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostDetailResponseDto {
    private Long postId;
    private String title;
    private String content;
    private String status;
    private LocalDateTime createdAt;
    private AuthorDetailResponseDto authorDetail;
    private AnimalDetailResponseDto animalDetail;
    List<PostImageResponseDto> postImage;
}
