package animals.demo.admin.controller;

import animals.demo.admin.dto.*;
import animals.demo.admin.service.AdminService;
import animals.demo.auth.dto.AdminLoginRequestDto;
import animals.demo.auth.dto.LoginResponseDto;
import animals.demo.common.ApiResponse;
import animals.demo.notice.dto.CreateNoticeRequestDto;
import animals.demo.notice.dto.CreateNoticeResponseDto;
import animals.demo.notice.dto.UpdateNoticeRequestDto;
import animals.demo.notice.service.NoticeService;
import animals.demo.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;
    private final NoticeService noticeService;

    //관리자 로그인
    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody AdminLoginRequestDto adminLoginRequestDto) {
        LoginResponseDto response = adminService.adminLogin(adminLoginRequestDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("관리자 로그인 되었습니다.", response));
    }

    //공지사항 작성
    @PostMapping("/notices")
    public ResponseEntity<?> createNotice(@RequestBody CreateNoticeRequestDto createNoticeRequestDto) {
        Long adminId = SecurityUtils.getCurrentUserId();
        CreateNoticeResponseDto response = noticeService.createNotice(adminId, createNoticeRequestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("공지사항이 정상적으로 작성되었습니다.", response));
    }

    //공지사항 수정
    @PutMapping("/notices/{noticeId}")
    public ResponseEntity<?> updateNotice(@PathVariable Long noticeId, @RequestBody UpdateNoticeRequestDto updateNoticeRequestDto) {
        Long adminId = SecurityUtils.getCurrentUserId();
        noticeService.updateNotice(noticeId, adminId, updateNoticeRequestDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("공지사항이 수정되었습니다.", null));
    }

    //공지사항 삭제
    @DeleteMapping("/notices/{noticeId}")
    public ResponseEntity<?> deleteNotice(@PathVariable Long noticeId) {
        Long adminId = SecurityUtils.getCurrentUserId();
        noticeService.deleteNotice(adminId, noticeId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("공지사항이 삭제되었습니다.", null));
    }

    //관리자용 보호소관리자 신청 목록 조회
    @GetMapping("/shelter-admin-requests")
    public ResponseEntity<?> getShelterRequestList() {
        Long adminId = SecurityUtils.getCurrentUserId();
        List<ShelterAdminRequestListResponseDto> response = adminService.getShelterRequestList(adminId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("보호소 관리자 신청 목록 조회에 성공했습니다.", response));
    }

    //관리자용 보호소관리자 신청 상세 조회
    @GetMapping("/shelter-admin-requests/{requestId}")
    public ResponseEntity<?> getShelterRequest(@PathVariable Long requestId) {
        Long adminId = SecurityUtils.getCurrentUserId();
        ShelterAdminRequestResponseDto response = adminService.getShelterRequest(adminId, requestId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("보호소 관리자 신청 상세 조회에 성공했습니다.", response));
    }

    //보호소 관리자 신청 승인
    @PostMapping("/shelter-admin-requests/{requestId}/approve")
    public ResponseEntity<?> approvedRequest(@PathVariable Long requestId) {
        Long adminId = SecurityUtils.getCurrentUserId();
        ApproveShelterAdminResponseDto response = adminService.approvedRequest(adminId, requestId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("보호소 관리자 신청이 승인되었습니다.", response));
    }

    //보호소 관리자 신청 반려
    @PostMapping("/shelter-admin-requests/{requestId}/reject")
    public ResponseEntity<?> rejectedRequest(@PathVariable Long requestId, @RequestBody RejectShelterAdminRequestDto rejectShelterAdminRequestDto) {
        Long adminId = SecurityUtils.getCurrentUserId();
        RejectShelterAdminResponseDto response = adminService.rejectedRequest(adminId, requestId, rejectShelterAdminRequestDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("보호소 관리자 신청이 반려되었습니다.", response));
    }
}

