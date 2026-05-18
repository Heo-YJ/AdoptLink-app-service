package animals.demo.post.repository;

import animals.demo.post.entity.Post;
import animals.demo.post.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthor_UserIdAndDeletedAtIsNull(Long userId);
    Page<Post> findByTitleContainingAndStatusAndDeletedAtIsNull(String keyword, Status status, Pageable pageable);
}
