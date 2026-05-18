package animals.demo.post.repository;

import animals.demo.post.entity.PostAnimal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostAnimalRepository extends JpaRepository<PostAnimal, Long> {
    Optional<PostAnimal> findByPost_PostId(Long postId);
}
