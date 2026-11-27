package com.submate.backend.subscription.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "subscriptions", indexes = {
        @Index(name = "idx_subscription_member", columnList = "member_id,product_id,end_date"),
        @Index(name = "idx_subscription_expiry", columnList = "status,end_date")})
@Getter
@NoArgsConstructor
public class Subscription {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id") private Long subscriptionId;
    @Column(name = "member_id", nullable = false) private Long memberId;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private SubscriptionStatus status;
    @Column(name = "start_date", nullable = false) private LocalDate startDate;
    @Column(name = "end_date", nullable = false) private LocalDate endDate;
    @Column(name = "cancelled_at") private LocalDateTime cancelledAt;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @Version private long version;

    public Subscription(Long memberId, Long productId, LocalDate startDate, LocalDate endDate, LocalDateTime now) {
        this.memberId = memberId; this.productId = productId;
        this.startDate = startDate; this.endDate = endDate;
        this.status = SubscriptionStatus.ACTIVE; this.createdAt = now; this.updatedAt = now;
    }

    public void markCancelled(LocalDateTime now) {
        status = SubscriptionStatus.CANCELLED; cancelledAt = now; updatedAt = now;
    }
    public void markExpired(LocalDateTime now) { status = SubscriptionStatus.EXPIRED; updatedAt = now; }
    public void endForRefund(LocalDate today, LocalDateTime now) {
        if (endDate.isAfter(today)) endDate = today;
        markExpired(now);
    }
}
