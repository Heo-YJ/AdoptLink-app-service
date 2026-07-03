package animals.demo.chat.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatRoomResponseDto {
    private Long roomId;
    private String nickname;
    private String thumbnailImageUrl;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private int unread_count;
}
