package com.submate.backend.notice;

import com.submate.backend.notice.dto.NoticeRequest;
import com.submate.backend.notice.dto.NoticeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    @Transactional(readOnly = true)
    public List<NoticeResponse> getNotices() {
        return noticeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NoticeResponse getNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() ->
                        new IllegalArgumentException("공지사항을 찾을 수 없습니다.")
                );

        return toResponse(notice);
    }

    @Transactional
    public NoticeResponse createNotice(NoticeRequest request) {
        Notice notice = new Notice(
                request.title(),
                request.content()
        );

        Notice saved = noticeRepository.save(notice);

        return toResponse(saved);
    }

    @Transactional
    public NoticeResponse updateNotice(
            Long noticeId,
            NoticeRequest request
    ) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() ->
                        new IllegalArgumentException("공지사항을 찾을 수 없습니다.")
                );

        notice.update(
                request.title(),
                request.content()
        );

        return toResponse(notice);
    }

    @Transactional
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() ->
                        new IllegalArgumentException("공지사항을 찾을 수 없습니다.")
                );

        noticeRepository.delete(notice);
    }

    private NoticeResponse toResponse(Notice notice) {
        return new NoticeResponse(
                notice.getNoticeId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}