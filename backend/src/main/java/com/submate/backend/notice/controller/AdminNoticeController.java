package com.submate.backend.notice.controller;

import com.submate.backend.notice.NoticeService;
import com.submate.backend.notice.dto.NoticeRequest;
import com.submate.backend.notice.dto.NoticeResponse;
import com.submate.backend.subscription.support.CurrentMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/notices")
public class AdminNoticeController {

    private final NoticeService noticeService;
    private final CurrentMember currentMember;

    @PostMapping
    public NoticeResponse createNotice(
            @Valid @RequestBody NoticeRequest request
    ) {
        currentMember.requireAdmin();
        return noticeService.createNotice(request);
    }

    @PatchMapping("/{noticeId}")
    public NoticeResponse updateNotice(
            @PathVariable Long noticeId,
            @Valid @RequestBody NoticeRequest request
    ) {
        currentMember.requireAdmin();
        return noticeService.updateNotice(noticeId, request);
    }

    @DeleteMapping("/{noticeId}")
    public void deleteNotice(
            @PathVariable Long noticeId
    ) {
        currentMember.requireAdmin();
        noticeService.deleteNotice(noticeId);
    }
}