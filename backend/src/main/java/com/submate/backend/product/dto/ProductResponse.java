package com.submate.backend.product.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long productId,
        Long categoryId,
        String categoryName,
        String name,
        BigDecimal price,
        String billingCycle,
        String status
) {
}