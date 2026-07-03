package animals.demo.notification.entity;

import animals.demo.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "notification_preferences")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationPreference {

    //알림ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    //알림 타입
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    //채널
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    //알림 여부
    @Column(nullable = false)
    private boolean enabled;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private NotificationPreference(
            User user,
            Type type,
            Channel channel,
            Boolean enabled
    ) {
        this.user = user;
        this.type = type;
        this.channel = channel;
        this.enabled = enabled;
    }

    //알림 켜기,끄기
    public void updateEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
