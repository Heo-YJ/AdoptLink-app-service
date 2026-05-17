package animals.demo.post.repository;

import animals.demo.post.entity.PostAnimal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostAnimalRepository extends JpaRepository<PostAnimal, Long> {
}
