package animals.demo.user.repository;

import animals.demo.user.entity.AuthProvider;
import animals.demo.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String loginId);
    Optional<User> findByNickname(String nickname);
    Optional<User> findByPhone(String phone);
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
}
