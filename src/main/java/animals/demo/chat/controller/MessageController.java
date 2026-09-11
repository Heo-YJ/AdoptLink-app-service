package animals.demo.chat.controller;

import animals.demo.chat.dto.MessageDto;
import animals.demo.chat.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/message")
    public void sendMessage(MessageDto messageDto, java.security.Principal principal) {
        if (principal == null) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required");
        }
        MessageDto savedMessage = messageService.saveMessage(Long.valueOf(principal.getName()), messageDto);
        messagingTemplate.convertAndSend(
                "/topic/chat/" + messageDto.getRoomId(),
                savedMessage
        );
    }




}
