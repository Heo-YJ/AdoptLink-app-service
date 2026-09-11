package animals.demo.chat;

import animals.demo.chat.dto.MessageDto;
import animals.demo.chat.entity.ChatRoom;
import animals.demo.chat.repository.*;
import animals.demo.chat.service.*;
import animals.demo.chat.websocket.config.ChatChannelInterceptor;
import animals.demo.common.CustomException;
import animals.demo.common.ErrorCode;
import animals.demo.post.entity.Post;
import animals.demo.user.entity.User;
import animals.demo.user.repository.*;
import animals.demo.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatRegressionTests {
    private User user(long id) {
        User user = mock(User.class);
        when(user.getUserId()).thenReturn(id);
        return user;
    }
    private ChatRoom room() {
        Post post = mock(Post.class);
        doReturn(user(2)).when(post).getAuthor();
        return ChatRoom.builder().inquiryUser(user(1)).post(post).build();
    }
    /*
    채팅방 생성 시 양쪽 참여자가 모두 멤버 테이블에 저장되는지 테스트
     */
    @Test void creatingRoomPersistsBothMembers() {
        var users = mock(UserRepository.class);
        var posts = mock(animals.demo.post.repository.PostRepository.class);
        var rooms = mock(ChatRoomRepository.class);
        var members = mock(ChatRoomMemberRepository.class);
        var service = new ChatRoomService(users, posts, rooms, mock(UserBlockRepository.class),
                mock(animals.demo.post.repository.PostImageRepository.class), members);
        doReturn(Optional.of(user(1))).when(users).findById(1L);
        doReturn(Optional.of(room().getPost())).when(posts).findById(7L);
        when(rooms.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var request = new animals.demo.chat.dto.CreateChatRoomRequestDto();
        ReflectionTestUtils.setField(request, "postId", 7L);
        assertTrue(service.createChat(1L, request).isNew());
        var captor = org.mockito.ArgumentCaptor.forClass(animals.demo.chat.entity.ChatRoomMember.class);
        verify(members, times(2)).save(captor.capture());
        assertEquals(List.of(1L, 2L), captor.getAllValues().stream().map(m -> m.getUser().getUserId()).toList());
    }

    /*
    권한 없는 사용자의 채팅방 접근을 차단하는 공통 검증 로직이 제대로 동작하는지 테스트
     */
    @Test void onlyParticipantsMayAccessRoom() {
        ChatRoom room = room();
        assertDoesNotThrow(() -> ChatRoomService.requireParticipant(room, 1L));
        assertDoesNotThrow(() -> ChatRoomService.requireParticipant(room, 2L));
        assertEquals(ErrorCode.FORBIDDEN, assertThrows(CustomException.class,
                () -> ChatRoomService.requireParticipant(room, 3L)).getErrorCode());
    }
    /*
    발신자 위조 방지:
        "누가 보냈는지는 클라이언트 payload가 아니라 인증 토큰 기준으로 결정한다." 라는
        보안 요구사항을 검증하는 테스트
     */
    @Test void authenticatedSenderOverridesPayloadAndUpdatesPreview() {
        var messages = mock(MessageRepository.class);
        var rooms = mock(ChatRoomRepository.class);
        var users = mock(UserRepository.class);
        var members = mock(ChatRoomMemberRepository.class);
        var blocks = mock(UserBlockRepository.class);
        var service = new MessageService(messages, rooms, users, members, blocks);
        ChatRoom room = room();
        when(rooms.findById(7L)).thenReturn(Optional.of(room));
        doReturn(Optional.of(user(1))).when(users).findById(1L);
        MessageDto dto = new MessageDto();
        ReflectionTestUtils.setField(dto, "roomId", 7L);
        ReflectionTestUtils.setField(dto, "content", "hello");
        dto.setSenderUserId(2L);
        //클라이언트가 보낸 senderUserId를 무시하고 강제 override
        service.saveMessage(1L, dto);
        assertEquals(1L, dto.getSenderUserId());
        assertEquals("hello", room.getLastMessage());
        assertNotNull(room.getLastMessageAt());
        assertThrows(CustomException.class, () -> service.saveMessage(3L, dto));
        verify(messages, times(1)).save(any());
    }

    /*
    페이지네이션 파라미터 검증:
        페이지/사이즈 값에 대한 입력 검증(경계값 테스트)
     */
    @Test void invalidPaginationReturnsBadRequest() {
        var rooms = mock(ChatRoomRepository.class);
        doReturn(Optional.of(room())).when(rooms).findById(7L);
        var service = new MessageService(mock(MessageRepository.class), rooms,
                mock(UserRepository.class), mock(ChatRoomMemberRepository.class), mock(UserBlockRepository.class));
        for (int[] values : new int[][]{{0, 0}, {-1, 10}, {0, 101}, {1, 10}}) {
            assertEquals(ErrorCode.BAD_REQUEST, assertThrows(CustomException.class,
                    () -> service.getMessages(1L, 7L, values[0], values[1])).getErrorCode());
        }
    }

    /*
    인증 없는 연결/직접 브로커 전송 차단
     */
    @Test void anonymousConnectAndDirectBrokerSendAreRejected() {
        var interceptor = new ChatChannelInterceptor(mock(JwtTokenProvider.class), mock(ChatRoomRepository.class));
        var connect = StompHeaderAccessor.create(StompCommand.CONNECT);
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(
                MessageBuilder.createMessage(new byte[0], connect.getMessageHeaders()), null));
        var send = StompHeaderAccessor.create(StompCommand.SEND);
        send.setUser(new UsernamePasswordAuthenticationToken(1L, null, List.of()));
        send.setDestination("/topic/chat/7");
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(
                MessageBuilder.createMessage(new byte[0], send.getMessageHeaders()), null));
    }

    /*
    JWT 검증 및 토큰 타입 제한:
        accessToken만 WebSocket 인증에 사용 가능하고, refreshToken으로는 연결할 수 없음을 검증
     */
    @Test void connectUsesVerifiedTokenIdentityAndRejectsRefreshToken() {
        var tokens = mock(JwtTokenProvider.class);
        when(tokens.getRole("access")).thenReturn("USER");
        when(tokens.getUserId("access")).thenReturn(1L);
        var interceptor = new ChatChannelInterceptor(tokens, mock(ChatRoomRepository.class));
        var connect = StompHeaderAccessor.create(StompCommand.CONNECT);
        connect.setNativeHeader("Authorization", "Bearer access");
        connect.setLeaveMutable(true);
        interceptor.preSend(MessageBuilder.createMessage(new byte[0], connect.getMessageHeaders()), null);
        assertEquals("1", connect.getUser().getName());
        verify(tokens).validateToken("access");
        var refresh = StompHeaderAccessor.create(StompCommand.CONNECT);
        refresh.setNativeHeader("Authorization", "Bearer refresh");
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(
                MessageBuilder.createMessage(new byte[0], refresh.getMessageHeaders()), null));
    }
    @Test void nonParticipantCannotSubscribe() {
        var rooms = mock(ChatRoomRepository.class);
        doReturn(Optional.of(room())).when(rooms).findById(7L);
        var interceptor = new ChatChannelInterceptor(mock(JwtTokenProvider.class), rooms);
        var subscribe = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        subscribe.setUser(new UsernamePasswordAuthenticationToken(3L, null, List.of()));
        subscribe.setDestination("/topic/chat/7");
        assertThrows(CustomException.class, () -> interceptor.preSend(
                MessageBuilder.createMessage(new byte[0], subscribe.getMessageHeaders()), null));
    }
}
