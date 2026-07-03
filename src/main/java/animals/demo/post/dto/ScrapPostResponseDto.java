package animals.demo.post.dto;

import animals.demo.post.entity.PostScrap;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScrapPostResponseDto {
    private Long postId;
    private String thumbnailImageUrl;
    private String title;
    private String contentPreview;

    public static ScrapPostResponseDto from(PostScrap scrap, String thumbnailImageUrl) {
        String content = scrap.getPost().getContent();
        String contentPreview = content.length() > 100
                ? content.substring(100 ) + "..."
                : content;
        return ScrapPostResponseDto.builder()
                .postId(scrap.getPost().getPostId())
                .thumbnailImageUrl(thumbnailImageUrl)
                .title(scrap.getPost().getTitle())
                .contentPreview(contentPreview)
                .build();
    }
}
