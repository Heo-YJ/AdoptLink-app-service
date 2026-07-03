package animals.demo.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admin_refresh_token")
@Getter
@NoArgsConstructor
public class AdminRefreshToken {

    @Id
    private Long adminId;

    private String refreshToken;

    @Builder
    public AdminRefreshToken(Long adminId, String refreshToken) {
        this.adminId = adminId;
        this.refreshToken = refreshToken;
    }
}