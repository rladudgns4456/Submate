package com.submate.backend.product;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    public Category(String name) {
        this.name = name;
    }

    public void changeName(String name) {
        this.name = name;
    }
}