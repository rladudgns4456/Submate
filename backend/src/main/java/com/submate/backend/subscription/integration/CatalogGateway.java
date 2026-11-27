package com.submate.backend.subscription.integration;

import com.submate.backend.subscription.support.DomainException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Member/Product 담당자의 테이블을 읽는 연결 지점. 해당 도메인의 CRUD는 소유하지 않는다. */
@Component
public class CatalogGateway {
    private final JdbcTemplate jdbc;
    public CatalogGateway(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public record MemberReference(Long memberId, String email, String role) {}
    public record ProductReference(Long productId, String name, BigDecimal price,
                                   String billingCycle, String status) {
        public LocalDate endDate(LocalDate start) {
            return switch (billingCycle) {
                case "MONTHLY" -> start.plusMonths(1);
                case "YEARLY" -> start.plusYears(1);
                default -> throw DomainException.conflict("지원하지 않는 결제 주기입니다.");
            };
        }
    }

    public MemberReference memberByEmail(String email) {
        return jdbc.query("select member_id, email, role from members where email = ?",
                (rs, row) -> new MemberReference(rs.getLong("member_id"), rs.getString("email"),
                        rs.getString("role")), email).stream().findFirst()
                .orElseThrow(() -> DomainException.unauthorized("인증된 회원을 찾을 수 없습니다."));
    }

    public void lockMember(Long memberId) {
        List<Long> ids = jdbc.query("select member_id from members where member_id = ? for update",
                (rs, row) -> rs.getLong(1), memberId);
        if (ids.isEmpty()) throw DomainException.notFound("회원");
    }

    public ProductReference product(Long productId) {
        return jdbc.query("select product_id, name, price, billing_cycle, status from products where product_id = ?",
                (rs, row) -> new ProductReference(rs.getLong("product_id"), rs.getString("name"),
                        rs.getBigDecimal("price"), rs.getString("billing_cycle"),
                        rs.getString("status")), productId).stream().findFirst()
                .orElseThrow(() -> DomainException.notFound("상품"));
    }

    public ProductReference lockProduct(Long productId) {
        return jdbc.query("select product_id, name, price, billing_cycle, status from products where product_id = ? for update",
                (rs, row) -> new ProductReference(rs.getLong("product_id"), rs.getString("name"),
                        rs.getBigDecimal("price"), rs.getString("billing_cycle"),
                        rs.getString("status")), productId).stream().findFirst()
                .orElseThrow(() -> DomainException.notFound("상품"));
    }
}
