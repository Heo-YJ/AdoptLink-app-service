package animals.demo.admin.entity;

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
@Table(name = "admins")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;

    //로그인ID
    @Column(length = 50, nullable = false)
    private String username;

    @Column(length = 255, nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean active = true;

    //생성날짜
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    //마지막 로그인
    private LocalDateTime lastLoginAt;

    @Builder
    private Admin(
            String username,
            String passwordHash,
            Boolean active
    ) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.active = active;
    }

    //관리자 비밀번호 변경
    public void changePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    //꼭 필요한건 아니긴한데..
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }
}
