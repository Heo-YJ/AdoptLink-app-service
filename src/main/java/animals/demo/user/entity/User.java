package animals.demo.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_provider_provider_id", columnNames = {"provider", "providerId"})
})
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(length = 50, unique = true)
    private String loginId;

    @Column(nullable = false, length = 20, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 20, unique = true)
    private String phone;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(length = 100)
    private String providerId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    @Builder
    private User(
            String loginId,
            String nickname,
            Role role,
            String passwordHash,
            String phone,
            String profileImageUrl,
            AuthProvider provider,
            String providerId
    ) {
        this.loginId = loginId;
        this.nickname = nickname;
        this.role = role;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.profileImageUrl = profileImageUrl;
        this.provider = provider;
        this.providerId = providerId;
    }

    //닉네임 수정
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    //핸드폰 번호 수정
    public void updatePhone(String phone) {
        this.phone = phone;
    }

    //프로필 사진 수정
    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    //비밀번호 변경
    public void changPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /*
    //보호소 관리자 요청 -> 따로 관리하는 엔티티에 넣는게 좋을듯!
    public void promoteToShelterAdmin() {
        this.role = Role.SHELTER_ADMIN;
    }
    */

    //회원탈퇴
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
