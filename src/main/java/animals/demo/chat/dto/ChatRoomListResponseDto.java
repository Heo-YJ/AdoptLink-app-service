package animals.demo.chat.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatRoomListResponseDto {
    private List<ChatRoomResponseDto> chatRooms;
}
