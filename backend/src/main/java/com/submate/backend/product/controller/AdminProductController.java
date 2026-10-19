package com.submate.backend.product.controller;

import com.submate.backend.product.ProductService;
import com.submate.backend.product.dto.ProductRequest;
import com.submate.backend.product.dto.ProductResponse;
import com.submate.backend.subscription.support.CurrentMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final ProductService productService;
    private final CurrentMember currentMember;

    @PostMapping
    public ProductResponse createProduct(
            @Valid @RequestBody ProductRequest request
    ) {
        currentMember.requireAdmin();
        return productService.createProduct(request);
    }

    @PatchMapping("/{productId}")
    public ProductResponse updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequest request
    ) {
        currentMember.requireAdmin();
        return productService.updateProduct(productId, request);
    }

    @PatchMapping("/{productId}/stop")
    public ProductResponse stopSellingProduct(
            @PathVariable Long productId
    ) {
        currentMember.requireAdmin();
        return productService.stopSellingProduct(productId);
    }
}