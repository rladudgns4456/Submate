package com.submate.backend.refund.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "refunds", uniqueConstraints = @UniqueConstraint(name = "uk_refund_payment", columnNames = "payment_id"))
@Getter
@NoArgsConstructor
public class Refund {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_id") private Long refundId;
    @Column(name = "payment_id", nullable = false) private Long paymentId;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal amount;
    @Column(length = 255, nullable = false) private String reason;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private RefundStatus status;
    @Column(name = "refunded_at") private LocalDateTime refundedAt;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Version private long version;
    // Nullable for historical requests created before prorated refunds were introduced.
    @Column(name = "total_days") private Long totalDays;
    @Column(name = "remaining_days") private Long remainingDays;

    public Refund(Long paymentId, BigDecimal amount, String reason, LocalDateTime now, long totalDays, long remainingDays) {
        this.paymentId = paymentId; this.amount = amount; this.reason = reason;
        this.status = RefundStatus.REQUESTED; this.createdAt = now;
        this.totalDays = totalDays; this.remainingDays = remainingDays;
    }
    public void complete(LocalDateTime now) { status = RefundStatus.COMPLETED; refundedAt = now; }
    public void reject() { status = RefundStatus.REJECTED; }
}
