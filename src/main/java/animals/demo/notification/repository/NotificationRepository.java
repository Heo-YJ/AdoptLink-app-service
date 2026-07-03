package animals.demo.notification.repository;

import animals.demo.notification.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationPreference, Long> {
    List<NotificationPreference> findByUser_UserId(Long userId);
}
