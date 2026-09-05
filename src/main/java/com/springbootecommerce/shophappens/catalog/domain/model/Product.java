package com.springbootecommerce.shophappens.catalog.domain.model;

import com.springbootecommerce.shophappens.catalog.domain.exception.InsufficientStockException;
import com.springbootecommerce.shophappens.catalog.domain.exception.ProductUnavailableException;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class Product {
    private final ProductId id;
    private final Sku sku;
    private String name;
    private String description;
    private Money price;
    private int stockQuantity;
    private String imageUrl;
    private boolean active;
    private final Set<CategoryId> categoryIds;

    private Product(
            ProductId id,
            Sku sku,
            String name,
            String description,
            Money price,
            int stockQuantity,
            String imageUrl,
            boolean active,
            Set<CategoryId> categoryIds) {
        this.id = id;
        this.sku = Objects.requireNonNull(sku);
        this.name = Objects.requireNonNull(name).strip();
        this.description = description;
        this.price = Objects.requireNonNull(price);
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.active = active;
        this.categoryIds = new HashSet<>(Set.copyOf(categoryIds));
    }

    public static Product create(
            Sku sku,
            String name,
            String description,
            Money price,
            int initialStock,
            String imageUrl,
            Set<CategoryId> categoryIds) {
        String normalizedName = normalizeName(name);
        Objects.requireNonNull(price, "price");
        if (initialStock < 0) {
            throw new IllegalArgumentException("Initial stock must not be negative");
        }
        return new Product(
                null,
                sku,
                normalizedName,
                description,
                price,
                initialStock,
                imageUrl,
                true,
                categoryIds);
    }

    public static Product restore(
            ProductId id,
            Sku sku,
            String name,
            String description,
            Money price,
            int stockQuantity,
            String imageUrl,
            boolean active,
            Set<CategoryId> categoryIds) {
        return new Product(
                Objects.requireNonNull(id),
                sku,
                name,
                description,
                price,
                stockQuantity,
                imageUrl,
                active,
                categoryIds);
    }

    public Optional<ProductId> id() {
        return Optional.ofNullable(id);
    }

    public Sku sku() {
        return sku;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Money price() {
        return price;
    }

    public int stockQuantity() {
        return stockQuantity;
    }

    public String imageUrl() {
        return imageUrl;
    }

    public boolean active() {
        return active;
    }

    public Set<CategoryId> categoryIds() {
        return Set.copyOf(categoryIds);
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public void reviseDetails(String name, String description, Money price, String imageUrl) {
        this.name = normalizeName(name);
        this.description = description;
        this.price = Objects.requireNonNull(price, "price");
        this.imageUrl = imageUrl;
    }

    public void replaceCategories(Set<CategoryId> categoryIds) {
        this.categoryIds.clear();
        this.categoryIds.addAll(Set.copyOf(categoryIds));
    }

    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity must not be negative");
        }
        this.stockQuantity = stockQuantity;
    }

    public PurchasedFacts purchase(int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("Quantity must be positive");
        if (!active) throw new ProductUnavailableException(id, sku);
        if (stockQuantity < quantity) {
            throw new InsufficientStockException(id, sku, quantity, stockQuantity);
        }
        stockQuantity -= quantity;
        return new PurchasedFacts(id, sku, name, price, quantity);
    }

    private static String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        return name.strip();
    }
}
