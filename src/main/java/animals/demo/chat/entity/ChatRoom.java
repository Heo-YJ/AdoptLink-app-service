package animals.demo.chat.entity;

import animals.demo.post.entity.Post;
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
@Table(name = "chat_rooms")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    //문의 시작 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiryUserId", nullable = false)
    private User inquiryUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Post post;

    //방 생성 날짜
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    //마지막 메시지 시각
    private LocalDateTime lastMessageAt;

    //마지막 메시지
    @Column(columnDefinition = "TEXT")
    private String lastMessage;

    @Builder
    private ChatRoom (
            User inquiryUser,
            Post post,
            String lastMessage
    ) {
        this.inquiryUser = inquiryUser;
        this.post = post;
        this.lastMessage = lastMessage;
    }

    // 마지막 메시지 기록
    public void updateLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
        this.lastMessageAt = LocalDateTime.now();
    }
}
