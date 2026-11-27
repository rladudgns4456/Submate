package com.submate.backend.refund.dto;
import com.submate.backend.refund.entity.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RefundResponse(Long refundId, Long paymentId, BigDecimal amount, String reason,
        RefundStatus status, LocalDateTime refundedAt, LocalDateTime createdAt, Long totalDays, Long remainingDays) {
    public static RefundResponse from(Refund r) {
        return new RefundResponse(r.getRefundId(), r.getPaymentId(), r.getAmount(), r.getReason(),
                r.getStatus(), r.getRefundedAt(), r.getCreatedAt(), r.getTotalDays(), r.getRemainingDays());
    }
}
