package animals.demo.post.repository;

import animals.demo.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthor_UserIdAndDeletedAtIsNull(Long userId);
}
