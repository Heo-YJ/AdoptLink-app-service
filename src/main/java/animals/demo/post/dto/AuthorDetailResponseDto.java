package animals.demo.post.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthorDetailResponseDto {
    private Long userId;
    private String nickname;
    private String profileImageUrl;
}
