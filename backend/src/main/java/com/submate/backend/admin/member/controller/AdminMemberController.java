package com.submate.backend.admin.member.controller;

import com.submate.backend.member.MemberService;
import com.submate.backend.member.dto.AdminMemberResponse;
import com.submate.backend.subscription.support.CurrentMember;
import org.springframework.web.bind.annotation.*;
import com.submate.backend.member.dto.AdminMemberUpdateRequest;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/admin/members")
public class AdminMemberController {

    private final MemberService memberService;
    private final CurrentMember current;

    public AdminMemberController(
            MemberService memberService,
            CurrentMember current
    ) {
        this.memberService = memberService;
        this.current = current;
    }

    @GetMapping
    public List<AdminMemberResponse> members() {
        current.requireAdmin();
        return memberService.getAllMembersForAdmin();
    }
    @GetMapping("/{memberId}")
public AdminMemberResponse memberDetail(
        @PathVariable Long memberId
) {
    current.requireAdmin();
    return memberService.getMemberForAdmin(memberId);
}
@PatchMapping("/{memberId}")
public AdminMemberResponse updateMember(
        @PathVariable Long memberId,
        @Valid @RequestBody AdminMemberUpdateRequest request
) {
    current.requireAdmin();
    return memberService.updateMemberForAdmin(memberId, request);
}
@PatchMapping("/{memberId}/withdraw")
public AdminMemberResponse withdrawMember(
        @PathVariable Long memberId
) {
    current.requireAdmin();
    return memberService.withdrawMemberForAdmin(memberId);
}
}