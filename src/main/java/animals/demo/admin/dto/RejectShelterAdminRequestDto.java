package animals.demo.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
// 보호소 관리자 신청 반려
public class RejectShelterAdminRequestDto {
    private String rejectReason;
}
