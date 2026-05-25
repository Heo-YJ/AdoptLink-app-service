package animals.demo.chat.service;

import animals.demo.chat.dto.MessageDto;
import animals.demo.chat.dto.MessageListResponseDto;
import animals.demo.chat.dto.MessageResponseDto;
import animals.demo.chat.dto.PageResponseDto;
import animals.demo.chat.entity.ChatRoom;
import animals.demo.chat.entity.Message;
import animals.demo.chat.repository.ChatRoomMemberRepository;
import animals.demo.chat.repository.ChatRoomRepository;
import animals.demo.chat.repository.MessageRepository;
import animals.demo.common.CustomException;
import animals.demo.common.ErrorCode;
import animals.demo.user.entity.User;
import animals.demo.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

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

        //채팅방 숨김상태일 때, 상대방이 메시지를 보내면 숨김=true -> 숨김=false로 전환
        Long inquiryUserId = chatRoom.getInquiryUser().getUserId();
        Long authorUserId = chatRoom.getPost().getAuthor().getUserId();
        Long receiverUserId = sender.getUserId().equals(inquiryUserId) ? authorUserId : inquiryUserId;

        chatRoomMemberRepository.findByRoom_RoomIdAndUser_UserId(messageDto.getRoomId(), receiverUserId)
                .ifPresent(member -> {
                    if(member.isHidden()) { member.show(); }
                });

        return messageDto;
    }

    //메시지 조회
    @Transactional
    public MessageListResponseDto getMessages(Long userId, Long roomId, int offset, int limit) {

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));

        Long inquiryUserId = chatRoom.getInquiryUser().getUserId();
        Long authorUserId = chatRoom.getPost().getAuthor().getUserId();

        if (!userId.equals(inquiryUserId) && !userId.equals(authorUserId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        Pageable pageable = PageRequest.of(
                offset / limit,
                limit
        );

        Page<Message> messages = messageRepository.findByChatRoom_RoomIdOrderByCreatedAtDesc(
                roomId,
                pageable
        );

        List<MessageResponseDto> messageList = messages.map(message -> {
            return MessageResponseDto.builder()
                    .messageId(message.getMessageId())
                    .senderUserId(message.getUser().getUserId())
                    .content(message.getContent())
                    .createdAt(message.getCreatedAt())
                    .build();
        }).toList();

        return MessageListResponseDto.builder()
                .messages(messageList)
                .pagination(PageResponseDto.builder()
                        .offset(offset)
                        .limit(limit)
                        .count(messageList.size())
                        .build())
                .build();
    }
}
