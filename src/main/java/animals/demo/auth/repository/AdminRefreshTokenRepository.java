package animals.demo.auth.repository;

import animals.demo.auth.entity.AdminRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRefreshTokenRepository extends JpaRepository<AdminRefreshToken, Long> {
    Optional<AdminRefreshToken> findByAdminId(Long adminId);
    void deleteByAdminId(Long adminId);
}
