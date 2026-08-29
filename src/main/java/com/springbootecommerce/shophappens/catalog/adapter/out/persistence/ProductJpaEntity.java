package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
@Entity
@Table(name = "product")
public class ProductJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(length = 2048)
    private String description;

    @Column
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToMany
    @JoinTable(
            name = "product_category",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<CategoryJpaEntity> categories = new LinkedHashSet<>();

    @Version private Long version;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    public static ProductJpaEntity create(
            String sku,
            String name,
            String description,
            BigDecimal price,
            int initialStock,
            String imageUrl,
            boolean active) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        if (price == null) {
            throw new IllegalArgumentException("Price must not be null");
        }
        if (initialStock < 0) {
            throw new IllegalArgumentException("Initial stock must not be negative");
        }
        var product = new ProductJpaEntity();
        product.sku = sku;
        product.name = name;
        product.description = description;
        product.price = price;
        product.stockQuantity = initialStock;
        product.imageUrl = imageUrl;
        product.active = active;
        return product;
    }
}
