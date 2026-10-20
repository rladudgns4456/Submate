package com.submate.backend.member.dto;

public record AdminMemberResponse(
        Long memberId,
        String email,
        String role,
        String status
) {
}