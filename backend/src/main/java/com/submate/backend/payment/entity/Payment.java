package com.submate.backend.payment.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "payments", uniqueConstraints =
        @UniqueConstraint(name = "uk_payment_request", columnNames = {"member_id", "request_key"}))
@Getter
@NoArgsConstructor
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id") private Long paymentId;
    @Column(name = "subscription_id", nullable = false) private Long subscriptionId;
    @Column(name = "member_id", nullable = false) private Long memberId;
    @Column(name = "request_key", nullable = false, length = 64) private String requestKey;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal amount;
    @Enumerated(EnumType.STRING) @Column(name = "payment_method", nullable = false, length = 30) private PaymentMethod paymentMethod;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private PaymentStatus status;
    @Column(name = "paid_at") private LocalDateTime paidAt;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Version private long version;

    public Payment(Long subscriptionId, Long memberId, String requestKey, BigDecimal amount,
                   PaymentMethod paymentMethod, LocalDateTime now) {
        this.subscriptionId = subscriptionId; this.memberId = memberId; this.requestKey = requestKey;
        this.amount = amount; this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.COMPLETED; this.paidAt = now; this.createdAt = now;
    }
    public void markRefunded() { status = PaymentStatus.REFUNDED; }
}
