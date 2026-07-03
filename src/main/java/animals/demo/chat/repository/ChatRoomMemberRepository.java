package animals.demo.chat.repository;

import animals.demo.chat.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    Optional<ChatRoomMember> findByChatRoom_RoomIdAndUser_UserId(Long roomId, Long userId);
}
