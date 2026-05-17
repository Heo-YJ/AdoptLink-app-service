package animals.demo.post.repository;

import animals.demo.post.entity.Post;
import animals.demo.post.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    Optional<PostImage> findFirstByPost_PostIdAndOrderIndex(Long postId, int orderIndex);
}
