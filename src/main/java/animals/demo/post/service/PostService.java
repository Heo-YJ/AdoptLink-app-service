package animals.demo.post.service;

import animals.demo.common.CustomException;
import animals.demo.common.ErrorCode;
import animals.demo.post.dto.*;
import animals.demo.post.entity.*;
import animals.demo.post.repository.PostAnimalRepository;
import animals.demo.post.repository.PostImageRepository;
import animals.demo.post.repository.PostRepository;
import animals.demo.post.repository.PostScrapRepository;
import animals.demo.user.entity.User;
import animals.demo.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final PostScrapRepository postScrapRepository;
    private final PostAnimalRepository postAnimalRepository;

    //게시글 피드 조회 (내 게시글, 특정 유저 게시글 공통)
    @Transactional
    public PostFeedListResponseDto getPostFeed(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Post> posts = postRepository.findByAuthor_UserIdAndDeletedAtIsNull(userId);

        List<PostFeedResponseDto> postList = posts.stream()
                .map(post -> {
                    String thumbnailImageUrl = postImageRepository
                            .findFirstByPost_PostIdAndOrderIndex(post.getPostId(), 1)
                            .map(postImage -> postImage.getPostImageUrl())
                            .orElse(null);
                    return PostFeedResponseDto.builder()
                            .postId(post.getPostId())
                            .thumbnailImageUrl(thumbnailImageUrl)
                            .build();
                })
                .toList();

        return PostFeedListResponseDto.builder()
                .posts(postList)
                .build();
    }

    //스크랩 게시글 목록 조회
    @Transactional
    public ScrapPostListResponseDto getScrapPost(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<PostScrap> scraps = postScrapRepository.findByUser_UserId(userId);

        List<ScrapPostResponseDto> scrapList = scraps.stream()
                .map(scrap -> {
                    String thumbnailImageUrl = postImageRepository
                            .findFirstByPost_PostIdAndOrderIndex(scrap.getPost().getPostId(), 1)
                            .map(postImage -> postImage.getPostImageUrl())
                            .orElse(null);
                    return ScrapPostResponseDto.from(scrap, thumbnailImageUrl);
                })
                .toList();

        return ScrapPostListResponseDto.builder()
                .scrapPosts(scrapList)
                .build();
    }

    //새 게시글 작성
    @Transactional
    public CreatePostResponseDto createPost(Long userId
            , CreatePostRequestDto createPostRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        //Post 엔티티 생성 후 저장
        Post post = Post.builder()
                .author(user)
                .title(createPostRequestDto.getTitle())
                .content(createPostRequestDto.getContent())
                .status(Status.AVAILABLE)
                .build();

        Post savedPost = postRepository.save(post);

        //PostAnimal 엔티티 생성 후 저장
        PostAnimal postAnimal = PostAnimal.builder()
                .post(savedPost)
                .species(Species.valueOf(createPostRequestDto.getAnimalInfo().getSpecies()))
                .breed(createPostRequestDto.getAnimalInfo().getBreed())
                .sex(Sex.valueOf(createPostRequestDto.getAnimalInfo().getSex()))
                .age(createPostRequestDto.getAnimalInfo().getAge())
                .neutered(createPostRequestDto.getAnimalInfo().getNeutered())
                .build();

        postAnimalRepository.save(postAnimal);

        //이미지는 S3에 업로드 후 URL 받아서 PostImage 엔티티 저장
        List<ImageIndexRequestDto> images = createPostRequestDto.getImages();

        if (images != null) {
            for (ImageIndexRequestDto image : images) {
                PostImage postImage = PostImage.builder()
                        .post(savedPost)
                        .postImageUrl(image.getPostImageUrl())
                        .orderIndex(image.getOrderIndex())
                        .build();
                postImageRepository.save(postImage);
            }
        }

        //CreateResponseDto 반환
        return CreatePostResponseDto.builder()
                .postId(savedPost.getPostId())
                .title(savedPost.getTitle())
                .build();
    }

    //게시글 분양여부 변경
    @Transactional
    public ChangePostStatusResponseDto changeStatus(Long userId, Long postId
            , ChangePostStatusRequestDto changePostStatusRequestDto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (!post.getAuthor().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        post.changeStatus(Status.valueOf(changePostStatusRequestDto.getStatus()));

        return ChangePostStatusResponseDto.builder()
                .postId(post.getPostId())
                .status(post.getStatus().name())
                .build();
    }

    //게시글 수정
    //분양 사기와 같은 악용을 방지하기 위해 제목, 본문, 사진만 수정가능함 -> 추후 코드 수정 예정. 현재는 응답 데이터 null
    @Transactional
    public void updatePost(Long postId, Long userId, UpdatePostRequestDto updatePostRequestDto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (!post.getAuthor().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        post.updateTitle(updatePostRequestDto.getTitle());
        post.updateContent(updatePostRequestDto.getContent());

        PostAnimal postAnimal = postAnimalRepository.findByPost_PostId(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        if (updatePostRequestDto.getAnimal() != null) {
            postAnimal.update(
                    Species.valueOf(updatePostRequestDto.getAnimal().getSpecies()),
                    updatePostRequestDto.getAnimal().getBreed(),
                    Sex.valueOf(updatePostRequestDto.getAnimal().getSex()),
                    updatePostRequestDto.getAnimal().getAge(),
                    updatePostRequestDto.getAnimal().getNeutered()
            );
        }

        postImageRepository.deleteAllByPost_PostId(postId);
        if (updatePostRequestDto.getImages() != null) {
            for (ImageIndexRequestDto image : updatePostRequestDto.getImages()) {
                PostImage postImage = PostImage.builder()
                        .post(post)
                        .postImageUrl(image.getPostImageUrl())
                        .orderIndex(image.getOrderIndex())
                        .build();
                postImageRepository.save(postImage);
            }
        }
    }

    //게시글 삭제
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (!post.getAuthor().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        post.softDelete();
    }

    //게시글 상세 조회
    @Transactional
    public PostDetailResponseDto getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        User author = post.getAuthor();

        AuthorDetailResponseDto authorDetail = AuthorDetailResponseDto.builder()
                .userId(author.getUserId())
                .nickname(author.getNickname())
                .profileImageUrl(author.getProfileImageUrl())
                .build();

        PostAnimal postAnimal = postAnimalRepository.findByPost_PostId(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        AnimalDetailResponseDto animalDetail = AnimalDetailResponseDto.builder()
                .species(postAnimal.getSpecies().name())
                .breed(postAnimal.getBreed())
                .sex(postAnimal.getSex().name())
                .age(postAnimal.getAge())
                .neutered(postAnimal.isNeutered())
                .build();

        List<PostImageResponseDto> images = postImageRepository.findByPost_PostIdOrderByOrderIndexAsc(postId)
                .stream()
                .map(image -> PostImageResponseDto.builder()
                        .postImageUrl(image.getPostImageUrl())
                        .orderIndex(image.getOrderIndex())
                        .build())
                .toList();


        return PostDetailResponseDto.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .status(post.getStatus().name())
                .createdAt(post.getCreatedAt())
                .authorDetail(authorDetail)
                .animalDetail(animalDetail)
                .postImage(images)
                .build();
    }


    //게시글 검색
    @Transactional
    public PostSearchListResponseDto searchPosts(String keyword, int offset, int limit, String sort) {
        Pageable pageable = PageRequest.of(
                offset / limit,
                limit,
                sort.equals("oldest") ? Sort.by("createdAt").ascending() : Sort.by("createdAt").descending()
        );

        Page<Post> posts = postRepository.findByTitleContainingAndStatusAndDeletedAtIsNull(
                keyword,
                Status.AVAILABLE,
                pageable
        );

        List<PostSearchResponseDto> postList = posts.map(post -> {
            String thumbnailImageUrl = postImageRepository
                    .findFirstByPost_PostIdAndOrderIndex(post.getPostId(), 1)
                    .map(PostImage::getPostImageUrl)
                    .orElse(null);
            return PostSearchResponseDto.builder()
                    .postId(post.getPostId())
                    .title(post.getTitle())
                    .thumbnailImageUrl(thumbnailImageUrl)
                    .status(post.getStatus().name())
                    .createdAt(post.getCreatedAt())
                    .build();

        }).toList();

        return PostSearchListResponseDto.builder()
                .posts(postList)
                .pagination(PaginationResponseDto.builder()
                        .offset(offset)
                        .limit(limit)
                        .count(postList.size())
                        .build())
                .build();
    }

    //게시글 스크랩
    @Transactional
    public ScrapResponseDto scrap(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Optional<PostScrap> existingScrap = postScrapRepository.findByUser_UserIdAndPost_PostId(userId, postId);

        if (existingScrap.isPresent()) {
            postScrapRepository.delete(existingScrap.get());
            return ScrapResponseDto.builder()
                    .scraped(false)
                    .postId(post.getPostId())
                    .build();
        } else {
            PostScrap postScrap = PostScrap.builder()
                    .user(user)
                    .post(post)
                    .build();
            postScrapRepository.save(postScrap);
            return ScrapResponseDto.builder()
                    .scraped(true)
                    .postId(post.getPostId())
                    .build();
        }
    }
}
