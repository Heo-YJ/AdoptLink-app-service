package animals.demo.chat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatResponseDto {
    private Long roomId;
    private String nickname;
    private String thumbnailImageUrl;
}
