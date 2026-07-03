package animals.demo.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
//관리자용 보호소관리자 신청 상세 조회
public class ShelterAdminRequestResponseDto {
    private Long requestId;
    private Long userId;
    private String nickname;
    private Long facilityId;
    private String facilityName;
    private String proofImageUrl;
    private String status;
    private String rejectReason;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
