package com.submate.backend.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(

        @NotNull
        Long categoryId,

        @NotBlank
        @Size(max = 100)
        String name,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal price,

        @NotBlank
        @Pattern(
                regexp = "MONTHLY|YEARLY",
                message = "billingCycle은 MONTHLY 또는 YEARLY만 가능합니다."
        )
        String billingCycle

) {
}