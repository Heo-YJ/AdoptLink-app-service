package animals.demo.chat.repository;

import animals.demo.chat.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByChatRoom_RoomIdOrderByCreatedAtDesc(Long roomId, Pageable pageable);
}
