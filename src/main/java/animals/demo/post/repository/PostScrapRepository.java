package animals.demo.post.repository;

import animals.demo.post.entity.PostScrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostScrapRepository extends JpaRepository<PostScrap, Long> {
    List<PostScrap> findByUser_UserId(Long userId);
    Optional<PostScrap> findByUser_UserIdAndPost_PostId(Long userId, Long postId);
}