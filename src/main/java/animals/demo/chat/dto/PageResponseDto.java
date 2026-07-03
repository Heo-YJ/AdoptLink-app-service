package animals.demo.chat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PageResponseDto {
    private int offset;
    private int limit;
    private int count;
}
