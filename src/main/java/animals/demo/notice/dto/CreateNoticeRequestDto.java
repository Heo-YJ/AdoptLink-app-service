package animals.demo.notice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateNoticeRequestDto {
    private String title;
    private String content;
    private Boolean pinned;
}
