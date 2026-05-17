package animals.demo.post.repository;

import animals.demo.post.entity.PostScrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostScrapRepository extends JpaRepository<PostScrap, Long> {
    List<PostScrap> findByUser_UserId(Long userId);
}