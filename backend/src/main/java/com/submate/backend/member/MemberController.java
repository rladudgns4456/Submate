package com.submate.backend.member;

import com.submate.backend.member.dto.LoginRequest;
import com.submate.backend.member.dto.SignupRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.submate.backend.member.dto.ChangePasswordRequest;
import org.springframework.security.core.Authentication;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        Long memberId = memberService.signup(request);

        return ResponseEntity.ok(
                Map.of(
                        "message", "회원가입이 완료되었습니다.",
                        "memberId", memberId
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        String token = memberService.login(request);

        return ResponseEntity.ok(
                Map.of(
                        "message", "로그인이 완료되었습니다.",
                        "token", token
                )
        );
    }

    @GetMapping("/me")
public ResponseEntity<Map<String, Object>> getMyInfo(
        org.springframework.security.core.Authentication authentication
) {
    Member member = memberService.getMyInfo(authentication.getName());

    return ResponseEntity.ok(
            Map.of(
                    "memberId", member.getMemberId(),
                    "email", member.getEmail(),
                    "role", member.getRole()
            )
        );
    }
    @PatchMapping("/me/password")
public ResponseEntity<Map<String, String>> changePassword(
        Authentication authentication,
        @Valid @RequestBody ChangePasswordRequest request
) {
    memberService.changePassword(
            authentication.getName(),
            request
    );

    return ResponseEntity.ok(
            Map.of(
                    "message", "비밀번호가 변경되었습니다."
            )
    );
  }
}