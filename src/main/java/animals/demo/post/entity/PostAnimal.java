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
@Table(name = "post_animals")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostAnimal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postAnimalId;

    @OneToOne
    @JoinColumn(name = "postId")
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Species species;

    @Column(length = 100)
    private String breed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sex sex;

    private Integer age;

    @Column(nullable = false)
    private boolean neutered;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private PostAnimal(
            Post post,
            Species species,
            String breed,
            Sex sex,
            Integer age,
            Boolean neutered
    ){
        this.post = post;
        this.species = species;
        this.breed = breed;
        this.sex = sex;
        this.age = age;
        this.neutered = neutered;
    }

    // 게시글 수정
    public void update(Species species, String breed, Sex sex, Integer age, boolean neutered) {
        this.species = species;
        this.breed = breed;
        this.sex = sex;
        this.age = age;
        this.neutered = neutered;
    }

}
