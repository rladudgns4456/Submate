package com.submate.backend.notice;

import com.submate.backend.notice.dto.NoticeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public List<NoticeResponse> getNotices() {
        return noticeService.getNotices();
    }

    @GetMapping("/{noticeId}")
    public NoticeResponse getNotice(
            @PathVariable Long noticeId
    ) {
        return noticeService.getNotice(noticeId);
    }
}