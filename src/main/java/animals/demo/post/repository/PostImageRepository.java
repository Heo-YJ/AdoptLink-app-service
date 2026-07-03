package animals.demo.post.repository;

import animals.demo.post.entity.Post;
import animals.demo.post.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    Optional<PostImage> findFirstByPost_PostIdAndOrderIndex(Long postId, int orderIndex);
    void deleteAllByPost_PostId(Long postId);
    List<PostImage> findByPost_PostIdOrderByOrderIndexAsc(Long postId);
}
