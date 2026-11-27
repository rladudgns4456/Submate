package com.submate.backend.subscription.dto;

import com.submate.backend.subscription.entity.*;
import java.time.*;

public record SubscriptionResponse(Long subscriptionId, Long memberId, Long productId,
        String productName, SubscriptionStatus status, LocalDate startDate, LocalDate endDate,
        LocalDateTime cancelledAt, boolean usable) {
    public static SubscriptionResponse from(Subscription s, String name, LocalDate today) {
        return new SubscriptionResponse(s.getSubscriptionId(), s.getMemberId(), s.getProductId(), name,
                s.getStatus(), s.getStartDate(), s.getEndDate(), s.getCancelledAt(),
                s.getStatus() != SubscriptionStatus.EXPIRED && !today.isBefore(s.getStartDate()) && today.isBefore(s.getEndDate()));
    }
}
