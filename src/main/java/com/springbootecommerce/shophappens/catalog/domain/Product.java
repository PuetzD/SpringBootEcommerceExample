package com.springbootecommerce.shophappens.catalog.domain;

import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;

@Getter
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(length = 2048)
    private String description;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "price", nullable = false))
    private Money price;

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
    private Set<Category> categories = new HashSet<>();

    @Version private Long version;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    protected Product() {}

    public static Product create(
            String sku,
            String name,
            String description,
            Money price,
            int initialStock,
            String imageUrl) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        Objects.requireNonNull(price, "price");
        if (initialStock < 0) {
            throw new IllegalArgumentException("Initial stock must not be negative");
        }
        var product = new Product();
        product.sku = sku;
        product.name = name;
        product.description = description;
        product.price = price;
        product.stockQuantity = initialStock;
        product.imageUrl = imageUrl;
        product.active = true;
        return product;
    }

    public void deactivate() {
        this.active = false;
    }

    public void reserveStock(int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("Quantity must be positive");
        if (!active) throw new ProductUnavailableException(id, sku);
        if (stockQuantity < quantity) {
            throw new InsufficientStockException(id, sku, quantity, stockQuantity);
        }
        stockQuantity -= quantity;
    }
}
