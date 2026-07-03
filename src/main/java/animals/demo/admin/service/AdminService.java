package animals.demo.admin.service;

import animals.demo.admin.dto.*;
import animals.demo.admin.entity.Admin;
import animals.demo.admin.repository.AdminRepository;
import animals.demo.auth.dto.AdminLoginRequestDto;
import animals.demo.auth.dto.LoginResponseDto;
import animals.demo.auth.entity.AdminRefreshToken;
import animals.demo.auth.repository.AdminRefreshTokenRepository;
import animals.demo.common.CustomException;
import animals.demo.common.ErrorCode;
import animals.demo.facility.entity.ShelterAdmin;
import animals.demo.facility.entity.ShelterAdminRequest;
import animals.demo.facility.repository.ShelterAdminRepository;
import animals.demo.facility.repository.ShelterAdminRequestRepository;
import animals.demo.security.JwtTokenProvider;
import animals.demo.user.entity.Role;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AdminRefreshTokenRepository adminRefreshTokenRepository;
    private final ShelterAdminRequestRepository shelterAdminRequestRepository;
    private final ShelterAdminRepository shelterAdminRepository;

    //관리자 로그인
    @Transactional
    public LoginResponseDto adminLogin(AdminLoginRequestDto adminLoginRequestDto) {

        //필수값 누락 확인
        if(adminLoginRequestDto.getUsername().isEmpty() || adminLoginRequestDto.getPassword().isEmpty()) {
            throw new CustomException(ErrorCode.MISSING_REQUIRED_VALUE);
        }

        //아아디 일치 확인
        Admin admin = adminRepository.findByUsername(adminLoginRequestDto.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.ID_PASSWORD_MISMATCH));

        //비밀번호 일치 확인
        if(!passwordEncoder.matches(adminLoginRequestDto.getPassword(), admin.getPasswordHash())) {
            throw new CustomException(ErrorCode.ID_PASSWORD_MISMATCH);
        }

        //accessToken, refreshToken 발급
        String accessToken = jwtTokenProvider.createAccessToken(admin.getAdminId(), admin.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(admin.getAdminId());

        //RefreshToken 저장
        AdminRefreshToken refreshTokenEntity = AdminRefreshToken.builder()
                .adminId(admin.getAdminId())
                .refreshToken(refreshToken)
                .build();
        adminRefreshTokenRepository.save(refreshTokenEntity);

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    //보호소 관리자 신청 목록 조회
    @Transactional
    public List<ShelterAdminRequestListResponseDto> getShelterRequestList(Long adminId) {
        adminRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_NOT_FOUND));

        return shelterAdminRequestRepository.findAll()
                .stream()
                .map(shelterAdminRequest -> ShelterAdminRequestListResponseDto.builder()
                        .requestId(shelterAdminRequest.getRequestId())
                        .userId(shelterAdminRequest.getUser().getUserId())
                        .nickname(shelterAdminRequest.getUser().getNickname())
                        .status(shelterAdminRequest.getStatus().name())
                        .createdAt(shelterAdminRequest.getCreatedAt())
                        .build())
                .toList();
    }

    //보호소 관리자 신청 상세 조회
    @Transactional
    public ShelterAdminRequestResponseDto getShelterRequest(Long adminId, Long requestId) {
        adminRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_NOT_FOUND));

        ShelterAdminRequest shelterAdminRequest = shelterAdminRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.REQUEST_NOT_FOUND));

        return ShelterAdminRequestResponseDto.builder()
                .requestId(shelterAdminRequest.getRequestId())
                .userId(shelterAdminRequest.getUser().getUserId())
                .nickname(shelterAdminRequest.getUser().getNickname())
                .facilityId(shelterAdminRequest.getFacility().getFacilityId())
                .facilityName(shelterAdminRequest.getFacility().getName())
                .proofImageUrl(shelterAdminRequest.getProofImageUrl())
                .status(shelterAdminRequest.getStatus().name())
                .rejectReason(shelterAdminRequest.getRejectReason())
                .createdAt(shelterAdminRequest.getCreatedAt())
                .reviewedAt(shelterAdminRequest.getReviewedAt())
                .build();
    }

    //보호소 관리자 신청 승인
    @Transactional
    public ApproveShelterAdminResponseDto approvedRequest(Long adminId, Long  requestId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_NOT_FOUND));

        ShelterAdminRequest request = shelterAdminRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.REQUEST_NOT_FOUND));

        if (!request.getStatus().name().equals("PENDING")) {
            throw new CustomException(ErrorCode.ALREADY_PROCESSED);
        }

        request.approve(admin);

        request.getUser().updateRole(Role.SHELTER_ADMIN);

        //shelter admin 등록
        ShelterAdmin shelterAdmin = ShelterAdmin.builder()
                .user(request.getUser())
                .facility(request.getFacility())
                .build();
        shelterAdminRepository.save(shelterAdmin);

        return ApproveShelterAdminResponseDto.builder()
                .requestId(request.getRequestId())
                .status(request.getStatus().name())
                .reviewedAt(request.getReviewedAt())
                .build();
    }

    //보호소 관리자 신청 반려
    @Transactional
    public RejectShelterAdminResponseDto rejectedRequest(Long adminId, Long requestId, RejectShelterAdminRequestDto rejectShelterAdminRequestDto) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_NOT_FOUND));

        ShelterAdminRequest request = shelterAdminRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.REQUEST_NOT_FOUND));

        if (!request.getStatus().name().equals("PENDING")) {
            throw new CustomException(ErrorCode.ALREADY_PROCESSED);
        }

        request.reject(admin, rejectShelterAdminRequestDto.getRejectReason());

        return RejectShelterAdminResponseDto.builder()
                .requestId(request.getRequestId())
                .status(request.getStatus().name())
                .rejectReason(request.getRejectReason())
                .reviewedAt(request.getReviewedAt())
                .build();
    }
}
