package com.submate.backend.payment.dto;

import com.submate.backend.payment.entity.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(Long paymentId, Long subscriptionId, BigDecimal amount,
        PaymentMethod paymentMethod, PaymentStatus status, LocalDateTime paidAt) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.getPaymentId(), p.getSubscriptionId(), p.getAmount(),
                p.getPaymentMethod(), p.getStatus(), p.getPaidAt());
    }
}
