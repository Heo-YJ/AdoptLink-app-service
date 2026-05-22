package animals.demo.chat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateChatRoomResponseDto {
    private Long postId;
    private Long roomId;
    private boolean isNew;
}
