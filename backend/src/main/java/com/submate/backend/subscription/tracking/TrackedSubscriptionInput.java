package com.submate.backend.subscription.tracking;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TrackedSubscriptionInput(
        @NotBlank @Size(max = 100) String name,
        @NotNull @Positive @Digits(integer = 9, fraction = 0) BigDecimal amount,
        @NotNull BillingCycle billingCycle,
        @NotNull LocalDate billingDate,
        @NotNull Boolean active,
        @Min(0) @Max(30) Integer reminderDays
) {
    @AssertTrue(message = "결제일은 1900년부터 9998년 사이여야 합니다.")
    public boolean isBillingDateInRange() {
        return billingDate == null || (billingDate.getYear() >= 1900 && billingDate.getYear() <= 9998);
    }
}
