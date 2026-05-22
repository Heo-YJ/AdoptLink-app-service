package animals.demo.chat.service;

import animals.demo.chat.dto.CreateChatRoomRequestDto;
import animals.demo.chat.dto.CreateChatRoomResponseDto;
import animals.demo.chat.entity.ChatRoom;
import animals.demo.chat.repository.ChatRoomRepository;
import animals.demo.common.CustomException;
import animals.demo.common.ErrorCode;
import animals.demo.post.entity.Post;
import animals.demo.post.repository.PostRepository;
import animals.demo.user.entity.User;
import animals.demo.user.repository.UserBlockRepository;
import animals.demo.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserBlockRepository userBlockRepository;

    //채팅방 생성
    @Transactional
    public CreateChatRoomResponseDto createChat(Long userId, CreateChatRoomRequestDto createChatRoomRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(createChatRoomRequestDto.getPostId())
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        //차단 여부 확인. 차단된 유저 관계는 채팅방을 생성할 수 없음
        Long authorId = post.getAuthor().getUserId();
        if (userBlockRepository.existsByBlocker_UserIdAndBlocked_UserId(userId, authorId)
                || userBlockRepository.existsByBlocker_UserIdAndBlocked_UserId(authorId, userId)) {
            throw new CustomException(ErrorCode.NOT_CREATE_CHATROOM);
        }

        //기존 채팅방이 존재하면 반환
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByInquiryUser_UserIdAndPost_PostId(
                userId, createChatRoomRequestDto.getPostId()
        );

        if (existingRoom.isPresent()) {
            return CreateChatRoomResponseDto.builder()
                    .postId(post.getPostId())
                    .roomId(existingRoom.get().getRoomId())
                    .isNew(false)
                    .build();
        }

        //존재하지 않으면 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .inquiryUser(user)
                .post(post)
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        return CreateChatRoomResponseDto.builder()
                .postId(post.getPostId())
                .roomId(savedRoom.getRoomId())
                .isNew(true)
                .build();
    }

    //채팅방 목록 조회
    @Transactional
    public

}
