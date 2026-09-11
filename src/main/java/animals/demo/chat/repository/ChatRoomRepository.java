package animals.demo.chat.repository;

import animals.demo.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByInquiryUser_UserIdAndPost_PostId(Long userId, Long postId);
    Optional<ChatRoom> findByRoomId(Long roomId);
    List<ChatRoom> findByInquiryUser_UserId(Long userId);

    @org.springframework.data.jpa.repository.Query("""
            select r from ChatRoom r
            where (r.inquiryUser.userId = :userId or r.post.author.userId = :userId)
              and not exists (select m.id from ChatRoomMember m
                  where m.chatRoom = r and m.user.userId = :userId and m.hidden = true)
            order by r.lastMessageAt desc, r.roomId desc
            """)
    List<ChatRoom> findVisibleRooms(@org.springframework.data.repository.query.Param("userId") Long userId);
}
