package com.submate.backend.product;

import com.submate.backend.product.dto.CategoryResponse;
import com.submate.backend.product.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return productService.getCategories();
    }

    @GetMapping("/products")
    public List<ProductResponse> products(
            @RequestParam(required = false) Long categoryId
    ) {
        return productService.getProducts(categoryId);
    }

    @GetMapping("/products/{productId}")
    public ProductResponse productDetail(
            @PathVariable Long productId
    ) {
        return productService.getProduct(productId);
    }
}