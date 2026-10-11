package com.submate.backend.notice.dto;

import java.time.LocalDateTime;

public record NoticeResponse(
        Long noticeId,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}