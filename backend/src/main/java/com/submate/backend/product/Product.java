package com.submate.backend.product;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "billing_cycle", nullable = false, length = 20)
    private String billingCycle;

    @Column(nullable = false, length = 20)
    private String status;

    public Product(
            Category category,
            String name,
            BigDecimal price,
            String billingCycle
    ) {
        this.category = category;
        this.name = name;
        this.price = price;
        this.billingCycle = billingCycle;
        this.status = "ACTIVE";
    }

    public void update(
            Category category,
            String name,
            BigDecimal price,
            String billingCycle
    ) {
        this.category = category;
        this.name = name;
        this.price = price;
        this.billingCycle = billingCycle;
    }

    public void stopSelling() {
        this.status = "INACTIVE";
    }
}