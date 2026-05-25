package animals.demo.chat.service;

import animals.demo.chat.dto.*;
import animals.demo.chat.entity.ChatRoom;
import animals.demo.chat.repository.ChatRoomRepository;
import animals.demo.common.CustomException;
import animals.demo.common.ErrorCode;
import animals.demo.post.entity.Post;
import animals.demo.post.entity.PostImage;
import animals.demo.post.repository.PostImageRepository;
import animals.demo.post.repository.PostRepository;
import animals.demo.user.entity.User;
import animals.demo.user.repository.UserBlockRepository;
import animals.demo.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserBlockRepository userBlockRepository;
    private final PostImageRepository postImageRepository;

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
    public ChatRoomListResponseDto getChatRoomList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<ChatRoom> chatRooms = chatRoomRepository.findByInquiryUser_UserId(userId);

        List<ChatRoomResponseDto> chatRoomList = chatRooms.stream()
                .map(chatRoom -> {
                    String thumbnailImageUrl = postImageRepository
                            .findFirstByPost_PostIdAndOrderIndex(chatRoom.getPost().getPostId(), 1)
                            .map(postImage -> postImage.getPostImageUrl())
                            .orElse(null);
                    return ChatRoomResponseDto.builder()
                            .roomId(chatRoom.getRoomId())
                            .nickname(chatRoom.getPost().getAuthor().getNickname())
                            .thumbnailImageUrl(thumbnailImageUrl)
                            .lastMessage(chatRoom.getLastMessage())
                            .lastMessageAt(chatRoom.getLastMessageAt())
                            .unread_count(0) //일단 하드코딩 해두고 나중에 제대로 구현
                            .build();
                }).toList();
        return ChatRoomListResponseDto.builder()
                .chatRooms(chatRoomList)
                .build();

    }

    //채팅방 상세 조회
    @Transactional
    public ChatResponseDto getChatRoom(Long userId, Long roomId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));

        String thumbnailImageUrl = postImageRepository
                .findFirstByPost_PostIdAndOrderIndex(chatRoom.getPost().getPostId(), 1)
                .map(PostImage::getPostImageUrl)
                .orElse(null);

        return ChatResponseDto.builder()
                .roomId(chatRoom.getRoomId())
                .nickname(chatRoom.getPost().getAuthor().getNickname())
                .thumbnailImageUrl(thumbnailImageUrl)
                .build();
    }

}
