package animals.demo.chat.entity;

import animals.demo.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "chat_room_members")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomMember {

    //멤버 매핑 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //채팅방ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roomId", nullable = false)
    private ChatRoom chatRoom;

    //참여 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    //마지막 읽은 메시지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lastReadMessageId")
    private Message lastReadMessage;

    //참여 날짜
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    //채팅 숨김 여부
    @Column(nullable = false)
    private boolean hidden;

    //숨긴 시각
    private LocalDateTime hiddenAt;

    @Builder
    private ChatRoomMember (
            ChatRoom chatRoom,
            User user,
            Message lastReadMessage,
            boolean hidden
    ) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.lastReadMessage = lastReadMessage;
        this.hidden = hidden;
    }

    //채팅 숨김
    public void hide() {
        this.hidden = true;
        this.hiddenAt = LocalDateTime.now();
    }

    //채팅 읽음 처리
    public void updateLastReadMessage(Message message) {
        if (message == null) {
            return;
        }
        if (this.lastReadMessage == null || message.getCreatedAt().isAfter(this.lastReadMessage.getCreatedAt())) {
            this.lastReadMessage = message;
        }
    }

}
