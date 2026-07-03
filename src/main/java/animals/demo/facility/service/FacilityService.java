package animals.demo.facility.service;

import animals.demo.common.CustomException;
import animals.demo.common.ErrorCode;
import animals.demo.facility.dto.ShelterAdminListResponseDto;
import animals.demo.facility.dto.ShelterAdminRequestDto;
import animals.demo.facility.dto.ShelterAdminResponseDto;
import animals.demo.facility.entity.Facility;
import animals.demo.facility.entity.ShelterAdmin;
import animals.demo.facility.entity.ShelterAdminRequest;
import animals.demo.facility.entity.Status;
import animals.demo.facility.repository.FacilityRepository;
import animals.demo.facility.repository.ShelterAdminRequestRepository;
import animals.demo.user.entity.User;
import animals.demo.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FacilityService {
    private final UserRepository userRepository;
    private final FacilityRepository facilityRepository;
    private final ShelterAdminRequestRepository shelterAdminRequestRepository;

    //보호소 관리자 신청
    @Transactional
    public ShelterAdminResponseDto shelterAdminRequest(Long userId, ShelterAdminRequestDto shelterAdminRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Facility facility = facilityRepository.findById(shelterAdminRequestDto.getFacilityId())
                .orElseThrow(() -> new CustomException(ErrorCode.FACILITY_NOT_FOUND));

        if (shelterAdminRequestRepository.existsByUser_UserIdAndFacility_FacilityId(userId, shelterAdminRequestDto.getFacilityId())) {
            throw new CustomException(ErrorCode.EXIST_REQUEST);
        }

        if (shelterAdminRequestDto.getProofImageUrl().isEmpty()) {
            throw new CustomException(ErrorCode.NULL_IMAGE);
        }

        // 신청 엔티티 저장
        ShelterAdminRequest request = ShelterAdminRequest.builder()
                .user(user)
                .facility(facility)
                .proofImageUrl(shelterAdminRequestDto.getProofImageUrl())
                .status(Status.PENDING)
                .build();

        ShelterAdminRequest savedRequest = shelterAdminRequestRepository.save(request);

        // 응답 반환
        return ShelterAdminResponseDto.builder()
                .requestId(savedRequest.getRequestId())
                .facilityId(savedRequest.getFacility().getFacilityId())
                .status(savedRequest.getStatus().name())
                .createdAt(savedRequest.getCreatedAt())
                .build();
    }

    // 보호소 관리자 신청 조회
    @Transactional
    public ShelterAdminListResponseDto getShelterAdminRequest(Long userId) {
        ShelterAdminRequest request = shelterAdminRequestRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.REQUEST_NOT_FOUND));

        return ShelterAdminListResponseDto.builder()
                .facilityId(request.getFacility().getFacilityId())
                .facilityName(request.getFacility().getName())
                .status(request.getStatus().name())
                .rejectReason(request.getRejectReason())
                .createdAt(request.getCreatedAt())
                .reviewedAt(request.getReviewedAt())
                .build();
    }

}
