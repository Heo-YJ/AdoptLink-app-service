package animals.demo.chat.dto;

import lombok.Getter;

@Getter
public class MessageDto {
    private Long roomId;
    private Long senderUserId;
    private String content;
}
