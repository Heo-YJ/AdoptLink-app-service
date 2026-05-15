package animals.demo.post.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyPostFeedListResponseDto {
    private List<MyPostFeedResponseDto> posts;
}
