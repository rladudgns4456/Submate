package com.submate.backend.member;

import com.submate.backend.member.auth.JwtProvider;
import com.submate.backend.member.dto.LoginRequest;
import com.submate.backend.member.dto.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.submate.backend.member.dto.ChangePasswordRequest;
import com.submate.backend.member.dto.AdminMemberResponse;
import java.util.List;
import com.submate.backend.member.dto.AdminMemberUpdateRequest;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public Long signup(SignupRequest request) {

        if (memberRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        String encodedPassword =
                passwordEncoder.encode(request.password());

        Member member = new Member(
                request.email(),
                encodedPassword
        );

        Member savedMember = memberRepository.save(member);

        return savedMember.getMemberId();
    }

    @Transactional(readOnly = true)
    public String login(LoginRequest request) {

        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.")
                );

        if (!passwordEncoder.matches(
                request.password(),
                member.getPassword()
        )) {
            throw new IllegalArgumentException(
                    "이메일 또는 비밀번호가 올바르지 않습니다."
            );
        }

        return jwtProvider.createToken(
                member.getEmail(),
                member.getRole()
        );
    }

    @Transactional(readOnly = true)
public Member getMyInfo(String email) {
    return memberRepository.findByEmail(email)
            .orElseThrow(() ->
                    new IllegalArgumentException("회원 정보를 찾을 수 없습니다.")
            );
    }
    @Transactional
public void changePassword(String email, ChangePasswordRequest request) {

    Member member = memberRepository.findByEmail(email)
            .orElseThrow(() ->
                    new IllegalArgumentException("회원 정보를 찾을 수 없습니다.")
            );

    if (!passwordEncoder.matches(
            request.currentPassword(),
            member.getPassword()
    )) {
        throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
    }

    String encodedNewPassword =
            passwordEncoder.encode(request.newPassword());

    member.changePassword(encodedNewPassword);
   }
   @Transactional(readOnly = true)
public List<AdminMemberResponse> getAllMembersForAdmin() {
    return memberRepository.findAll()
            .stream()
            .map(member -> new AdminMemberResponse(
        member.getMemberId(),
        member.getEmail(),
        member.getRole(),
        member.getStatus()
))
            .toList();
  }
  @Transactional(readOnly = true)
public AdminMemberResponse getMemberForAdmin(Long memberId) {
    Member member = memberRepository.findById(memberId)
            .orElseThrow(() ->
                    new IllegalArgumentException("회원을 찾을 수 없습니다.")
            );

    return new AdminMemberResponse(
        member.getMemberId(),
        member.getEmail(),
        member.getRole(),
        member.getStatus()
);
  }
  @Transactional
public AdminMemberResponse updateMemberForAdmin(
        Long memberId,
        AdminMemberUpdateRequest request
) {
    Member member = memberRepository.findById(memberId)
            .orElseThrow(() ->
                    new IllegalArgumentException("회원을 찾을 수 없습니다.")
            );

    if (!member.getEmail().equals(request.email())
            && memberRepository.existsByEmail(request.email())) {
        throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
    }

    member.changeEmail(request.email());

    return new AdminMemberResponse(
        member.getMemberId(),
        member.getEmail(),
        member.getRole(),
        member.getStatus()
);
}
@Transactional
public AdminMemberResponse withdrawMemberForAdmin(Long memberId) {

    Member member = memberRepository.findById(memberId)
            .orElseThrow(() ->
                    new IllegalArgumentException("회원을 찾을 수 없습니다.")
            );

    if ("WITHDRAWN".equals(member.getStatus())) {
        throw new IllegalArgumentException("이미 탈퇴 처리된 회원입니다.");
    }

    member.withdraw();

    return new AdminMemberResponse(
            member.getMemberId(),
            member.getEmail(),
            member.getRole(),
            member.getStatus()
    );
}
}