package com.submate.backend.admin.product.controller;

import com.submate.backend.subscription.support.CurrentMember;
import com.submate.backend.product.ProductService;
import com.submate.backend.product.dto.CategoryRequest;
import com.submate.backend.product.dto.CategoryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final ProductService productService;
    private final CurrentMember currentMember;

    @PostMapping
    public CategoryResponse createCategory(
            @Valid @RequestBody CategoryRequest request
    ) {
        currentMember.requireAdmin();
        return productService.createCategory(request);
    }

    @PatchMapping("/{categoryId}")
    public CategoryResponse updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequest request
    ) {
        currentMember.requireAdmin();
        return productService.updateCategory(categoryId, request);
    }

    @DeleteMapping("/{categoryId}")
    public void deleteCategory(
            @PathVariable Long categoryId
    ) {
        currentMember.requireAdmin();
        productService.deleteCategory(categoryId);
    }
}