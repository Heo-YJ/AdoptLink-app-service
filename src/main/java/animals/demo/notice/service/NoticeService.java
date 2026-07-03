package animals.demo.notice.service;

import animals.demo.admin.entity.Admin;
import animals.demo.admin.repository.AdminRepository;
import animals.demo.common.CustomException;
import animals.demo.common.ErrorCode;
import animals.demo.notice.dto.*;
import animals.demo.notice.entity.Notice;
import animals.demo.notice.repository.NoticeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final AdminRepository adminRepository;
    private final NoticeRepository noticeRepository;

    //공지사항 작성
    @Transactional
    public CreateNoticeResponseDto createNotice(Long adminId, CreateNoticeRequestDto createNoticeRequestDto) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_NOT_FOUND));

        Notice notice = Notice.builder()
                .admin(admin)
                .title(createNoticeRequestDto.getTitle())
                .content(createNoticeRequestDto.getContent())
                .pinned(createNoticeRequestDto.getPinned())
                .build();

        Notice savedNotice = noticeRepository.save(notice);

        return CreateNoticeResponseDto.builder()
                .noticeId(savedNotice.getNoticeId())
                .build();
    }

    //공지사항 수정
    @Transactional
    public void updateNotice(Long noticeId, Long adminId, UpdateNoticeRequestDto updateNoticeRequestDto) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));

        if(!notice.getAdmin().getAdminId().equals(adminId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        if(updateNoticeRequestDto.getTitle() != null) {
            notice.updateTitle(updateNoticeRequestDto.getTitle());
        }
        if(updateNoticeRequestDto.getContent() != null) {
            notice.updateContent(updateNoticeRequestDto.getContent());
        }
        if(updateNoticeRequestDto.getPinned() != null) {
            notice.updatePinned(updateNoticeRequestDto.getPinned());
        }
    }

    //공지사항 삭제
    @Transactional
    public void deleteNotice(Long adminId, Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));

        if(!notice.getAdmin().getAdminId().equals(adminId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        notice.softDelete();
    }

    //공지사항 목록 조회
    @Transactional
    public List<NoticeListResponseDto> getNoticeList() {
        return noticeRepository.findByDeletedAtIsNull()
                .stream()
                .map(notice -> NoticeListResponseDto.builder()
                        .noticeId(notice.getNoticeId())
                        .title(notice.getTitle())
                        .createdAt(notice.getCreatedAt())
                        .build())
                .toList();
    }

    //공지사항 상세 조회
    @Transactional
    public NoticeResponseDto getNotice(Long noticeId) {
        Notice notice = noticeRepository.findByNoticeIdAndDeletedAtIsNull(noticeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));

        return NoticeResponseDto.builder()
                .noticeId(notice.getNoticeId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }
}
