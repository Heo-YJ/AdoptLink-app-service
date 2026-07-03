package animals.demo.chat.repository;

import animals.demo.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByInquiryUser_UserIdAndPost_PostId(Long userId, Long postId);
    Optional<ChatRoom> findByRoomId(Long roomId);
    List<ChatRoom> findByInquiryUser_UserId(Long userId);

    Long roomId(Long roomId);
}
