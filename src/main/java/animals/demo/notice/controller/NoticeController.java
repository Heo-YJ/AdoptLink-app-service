package animals.demo.notice.controller;

import animals.demo.common.ApiResponse;
import animals.demo.notice.dto.NoticeListResponseDto;
import animals.demo.notice.dto.NoticeResponseDto;
import animals.demo.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeService noticeService;

    //공지사항 목록 조회
    @GetMapping
    public ResponseEntity<?> getNoticeList() {
        List<NoticeListResponseDto> response = noticeService.getNoticeList();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("공지사항 목록 조회에 성공했습니다.", response));
    }

    //공지사항 상세 조회
    @GetMapping("/{noticeId}")
    public ResponseEntity<?> getNotice(@PathVariable Long noticeId) {
        NoticeResponseDto response = noticeService.getNotice(noticeId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok("공지사항 상세 조회에 성공했습니다.", response));
    }
}
