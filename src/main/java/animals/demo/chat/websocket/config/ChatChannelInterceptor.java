package animals.demo.chat.websocket.config;

import animals.demo.chat.repository.ChatRoomRepository;
import animals.demo.chat.service.ChatRoomService;
import animals.demo.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatChannelInterceptor implements ChannelInterceptor {
    private final JwtTokenProvider tokenProvider;
    private final ChatRoomRepository chatRoomRepository;

    /*
    클라이언트가 보낸 채팅 요청을 처리하기 전에, 로그인 여부와 채팅방 접근 권한 확인
    “토큰으로 누구인지 확인하고, 서버가 허용한 경로와 자신이 참여한 방만 이용하게 만드는 관문”
     */
    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headers = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (headers == null) return message;
        if (StompCommand.CONNECT.equals(headers.getCommand())) {
            String authorization = headers.getFirstNativeHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new AccessDeniedException("Access token required");
            }
            String token = authorization.substring(7);
            tokenProvider.validateToken(token);
            // 관리자 ID는 일반 사용자 ID와 별도이며, refresh token에는 role이 없다.
            String role = tokenProvider.getRole(token);
            if (!"USER".equals(role) && !"SHELTER_ADMIN".equals(role)) {
                throw new AccessDeniedException("User access token required");
            }
            headers.setUser(new UsernamePasswordAuthenticationToken(
                    tokenProvider.getUserId(token), null, List.of()));
            //CONNECT에서 검증해 등록한 사용자 정보 공통 검사 (SNED, SUBSCRIBE)
        } else if (StompCommand.SEND.equals(headers.getCommand())
                || StompCommand.SUBSCRIBE.equals(headers.getCommand())) {
            if (headers.getUser() == null) throw new AccessDeniedException("Authentication required");
            String destination = headers.getDestination();
            //SEND: 허용한 전송 경로만 통과
            if (StompCommand.SEND.equals(headers.getCommand())) {
                if (!"/app/chat/message".equals(destination)) {
                    throw new AccessDeniedException("Unsupported destination");
                }
            } else { //SUBSCRIBE: 구독 경로와 방 참여 여부 검사
                if (destination == null || !destination.matches("/topic/chat/[0-9]+")) {
                    //여러 채팅방을 한꺼번에 받으려는 와일드카드 구독 차단
                    throw new AccessDeniedException("Unsupported subscription");
                }
                try {
                    Long roomId = Long.valueOf(destination.substring("/topic/chat/".length()));
                    var room = chatRoomRepository.findById(roomId)
                            .orElseThrow(() -> new AccessDeniedException("Chat room not found"));
                    ChatRoomService.requireParticipant(room, Long.valueOf(headers.getUser().getName()));
                } catch (NumberFormatException e) {
                    throw new AccessDeniedException("Invalid chat room", e);
                }
            }
        }
        return message;
    }
}
