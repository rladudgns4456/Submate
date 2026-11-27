package com.submate.backend.subscription.support;

import com.submate.backend.subscription.integration.CatalogGateway;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentMember {
    private final CatalogGateway catalog;
    public CurrentMember(CatalogGateway catalog) { this.catalog = catalog; }
    public Long memberId() { return member().memberId(); }

    public void requireAdmin() {
        var member = member();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean hasRole = auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!hasRole || !"ADMIN".equals(member.role())) throw DomainException.forbidden();
    }

    private CatalogGateway.MemberReference member() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw DomainException.unauthorized("로그인이 필요합니다.");
        }
        // JWT 통합 계약: Authentication.name = 회원 이메일, 권한 = ROLE_USER / ROLE_ADMIN.
        return catalog.memberByEmail(auth.getName());
    }
}
