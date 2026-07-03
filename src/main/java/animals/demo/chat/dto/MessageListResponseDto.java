package animals.demo.chat.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MessageListResponseDto {
    private List<MessageResponseDto> messages;
    private PageResponseDto pagination;
}
