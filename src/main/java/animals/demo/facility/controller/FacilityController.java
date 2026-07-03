package animals.demo.facility.controller;

import animals.demo.common.ApiResponse;
import animals.demo.facility.dto.ShelterAdminListResponseDto;
import animals.demo.facility.dto.ShelterAdminRequestDto;
import animals.demo.facility.dto.ShelterAdminResponseDto;
import animals.demo.facility.service.FacilityService;
import animals.demo.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shelter-admin-requests")
public class FacilityController {

    private final FacilityService facilityService;

    //보호소 관리자 신청
    @PostMapping
    public ResponseEntity<?> shelterAdminRequest(@RequestBody ShelterAdminRequestDto shelterAdminRequestDto) {
        Long userId = SecurityUtils.getCurrentUserId();
        ShelterAdminResponseDto response = facilityService.shelterAdminRequest(userId, shelterAdminRequestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("보호소 관리자 신청이 정상적으로 접수되었습니다.", response));
    }

    //보호소 관리자 신청 조회
    @GetMapping
    public ResponseEntity<?> getShelterAdminRequest() {
        Long userId = SecurityUtils.getCurrentUserId();
        ShelterAdminListResponseDto response = facilityService.getShelterAdminRequest(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("보호소 관리자 신청 목록 조회에 성공했습니다.", response));
    }
}
