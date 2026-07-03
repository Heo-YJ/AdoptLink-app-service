package animals.demo.facility.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ShelterAdminRequestDto {
    private Long facilityId;
    private String proofImageUrl;
}
