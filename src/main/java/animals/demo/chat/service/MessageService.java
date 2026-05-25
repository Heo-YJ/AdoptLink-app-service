package animals.demo.chat.service;

import animals.demo.chat.dto.MessageDto;
import animals.demo.chat.entity.ChatRoom;
import animals.demo.chat.entity.Message;
import animals.demo.chat.repository.ChatRoomRepository;
import animals.demo.chat.repository.MessageRepository;
import animals.demo.common.CustomException;
import animals.demo.common.ErrorCode;
import animals.demo.user.entity.User;
import animals.demo.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    @Transactional
    public MessageDto saveMessage(MessageDto messageDto) {
        ChatRoom chatRoom = chatRoomRepository.findById(messageDto.getRoomId())
                .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));

        User sender = userRepository.findById(messageDto.getSenderUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Message message = Message.builder()
                .chatRoom(chatRoom)
                .user(sender)
                .content(messageDto.getContent())
                .build();

        messageRepository.save(message);
        return messageDto;
    }
}
