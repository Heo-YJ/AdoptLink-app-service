package animals.demo.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
//관리자용 보호소관리자 신청 목록 조회
public class ShelterAdminRequestListResponseDto {
    private Long requestId;
    private Long userId;
    private String nickname;
    private String status;
    private LocalDateTime createdAt;
}
