package com.submate.backend.product;

import com.submate.backend.product.dto.CategoryRequest;
import com.submate.backend.product.dto.CategoryResponse;
import com.submate.backend.product.dto.ProductRequest;
import com.submate.backend.product.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(category -> new CategoryResponse(
                        category.getCategoryId(),
                        category.getName()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProducts(Long categoryId) {
        List<Product> products;

        if (categoryId == null) {
            products = productRepository.findAll();
        } else {
            products = productRepository.findByCategory_CategoryId(categoryId);
        }

        return products.stream()
                .map(this::toProductResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException("상품을 찾을 수 없습니다.")
                );

        return toProductResponse(product);
    }

    private ProductResponse toProductResponse(Product product) {
        return new ProductResponse(
                product.getProductId(),
                product.getCategory().getCategoryId(),
                product.getCategory().getName(),
                product.getName(),
                product.getPrice(),
                product.getBillingCycle(),
                product.getStatus()
        );
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() ->
                        new IllegalArgumentException("카테고리를 찾을 수 없습니다.")
                );

        Product product = new Product(
                category,
                request.name(),
                request.price(),
                request.billingCycle()
        );

        Product saved = productRepository.save(product);

        return toProductResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(
            Long productId,
            ProductRequest request
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException("상품을 찾을 수 없습니다.")
                );

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() ->
                        new IllegalArgumentException("카테고리를 찾을 수 없습니다.")
                );

        product.update(
                category,
                request.name(),
                request.price(),
                request.billingCycle()
        );

        return toProductResponse(product);
    }

    @Transactional
    public ProductResponse stopSellingProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException("상품을 찾을 수 없습니다.")
                );

        product.stopSelling();

        return toProductResponse(product);
    }
    @Transactional
public CategoryResponse createCategory(CategoryRequest request) {

    if (categoryRepository.existsByName(request.name())) {
        throw new IllegalArgumentException("이미 존재하는 카테고리입니다.");
    }

    Category category = new Category(request.name());
    Category saved = categoryRepository.save(category);

    return new CategoryResponse(
            saved.getCategoryId(),
            saved.getName()
    );
}

@Transactional
public CategoryResponse updateCategory(
        Long categoryId,
        CategoryRequest request
) {
    Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() ->
                    new IllegalArgumentException("카테고리를 찾을 수 없습니다.")
            );

    if (!category.getName().equals(request.name())
            && categoryRepository.existsByName(request.name())) {
        throw new IllegalArgumentException("이미 존재하는 카테고리입니다.");
    }

    category.changeName(request.name());

    return new CategoryResponse(
            category.getCategoryId(),
            category.getName()
    );
}

@Transactional
public void deleteCategory(Long categoryId) {

    Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() ->
                    new IllegalArgumentException("카테고리를 찾을 수 없습니다.")
            );

    if (!productRepository.findByCategory_CategoryId(categoryId).isEmpty()) {
        throw new IllegalArgumentException(
                "연결된 상품이 있는 카테고리는 삭제할 수 없습니다."
        );
    }

    categoryRepository.delete(category);
}
}