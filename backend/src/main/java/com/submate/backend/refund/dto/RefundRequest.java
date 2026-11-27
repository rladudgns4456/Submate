package com.submate.backend.refund.dto;
import jakarta.validation.constraints.*;
public record RefundRequest(@NotNull @Positive Long paymentId, @NotBlank @Size(max = 255) String reason) {}
