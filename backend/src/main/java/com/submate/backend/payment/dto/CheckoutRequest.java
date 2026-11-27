package com.submate.backend.payment.dto;

import com.submate.backend.payment.entity.PaymentMethod;
import jakarta.validation.constraints.*;

public record CheckoutRequest(
        @NotNull @Positive Long productId,
        @NotNull PaymentMethod paymentMethod,
        @NotBlank @Size(max = 64) @Pattern(regexp = "[A-Za-z0-9_-]+") String requestKey,
        @NotNull Boolean simulateFailure) {}
