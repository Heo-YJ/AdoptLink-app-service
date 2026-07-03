package animals.demo.chat.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MessageResponseDto {
    private Long messageId;
    private Long senderUserId;
    private String content;
    private LocalDateTime createdAt;
}
