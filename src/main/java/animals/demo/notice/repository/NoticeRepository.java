package animals.demo.notice.repository;

import animals.demo.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findByDeletedAtIsNull();
    Optional<Notice> findByNoticeIdAndDeletedAtIsNull(Long noticeId);
}
