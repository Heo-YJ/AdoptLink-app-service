package animals.demo.post.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
/*
* Page<> 대신 구현한 이유 *
* 내가 원하는 response 형식을 구현하려는 목적
* 클라이언에게 필요한 Offset, limit, count만 응답하기 위해 커스텀
* 나중에 cursor 기반 페이지네이션으로 전환 시 이 DTO만 수정하면 됨
 */
public class PaginationResponseDto {
    private int offset;
    private int limit;
    private int count;
}
