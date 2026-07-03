package animals.demo.user.repository;

import animals.demo.user.entity.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {
    // A가 B를 차단했는지 확인
    boolean existsByBlocker_UserIdAndBlocked_UserId(Long blockerId, Long blockedId);
}
