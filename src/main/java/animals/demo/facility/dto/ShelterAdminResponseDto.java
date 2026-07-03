package animals.demo.facility.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ShelterAdminResponseDto {
    private Long requestId;
    private Long facilityId;
    private String status;
    private LocalDateTime createdAt;
}
