package animals.demo.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
// 보호소 관리자 신청 반려
public class RejectShelterAdminResponseDto {
    private Long requestId;
    private String status;
    private String rejectReason;
    private LocalDateTime reviewedAt;
}
