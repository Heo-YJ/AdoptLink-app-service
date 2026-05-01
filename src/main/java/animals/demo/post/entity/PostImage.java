package animals.demo.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "post_images")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postId", nullable = false)
    private Post post;

    @Column(length = 500, name = "post_image_url", nullable = false)
    private String postImageUrl;

    @Column(nullable = false)
    private Integer orderIndex;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private PostImage(
            Post post,
            String postImageUrl,
            Integer orderIndex
    ){
        this.post = post;
        this.postImageUrl = postImageUrl;
        this.orderIndex = orderIndex;
    }

    // 이미지 수정 (개별수정이 아닌 전체수정이라 Service에서 다룰 예정)
    public void updateOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }
}
