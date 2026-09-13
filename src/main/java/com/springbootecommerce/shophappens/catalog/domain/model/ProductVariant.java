package com.springbootecommerce.shophappens.catalog.domain.model;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.util.Objects;
import java.util.Optional;

public final class ProductVariant {
    private final ProductVariantId id;
    private Sku sku;
    private Money price;
    private int stockQuantity;
    private String imageUrl;
    private boolean active;
    private final boolean defaultVariant;

    private ProductVariant(
            ProductVariantId id,
            Sku sku,
            Money price,
            int stockQuantity,
            String imageUrl,
            boolean active,
            boolean defaultVariant) {
        this.id = id;
        this.sku = Objects.requireNonNull(sku);
        this.price = Objects.requireNonNull(price);
        setStockQuantity(stockQuantity);
        this.imageUrl = imageUrl;
        this.active = active;
        this.defaultVariant = defaultVariant;
    }

    public static ProductVariant create(
            Sku sku,
            Money price,
            int stockQuantity,
            String imageUrl,
            boolean active,
            boolean defaultVariant) {
        return new ProductVariant(
                null, sku, price, stockQuantity, imageUrl, active, defaultVariant);
    }

    public static ProductVariant restore(
            ProductVariantId id,
            Sku sku,
            Money price,
            int stockQuantity,
            String imageUrl,
            boolean active,
            boolean defaultVariant) {
        return new ProductVariant(
                Objects.requireNonNull(id),
                sku,
                price,
                stockQuantity,
                imageUrl,
                active,
                defaultVariant);
    }

    public Optional<ProductVariantId> id() {
        return Optional.ofNullable(id);
    }

    public Sku sku() {
        return sku;
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

    public boolean isDefault() {
        return defaultVariant;
    }

    public void reviseCommercialDetails(Money price, String imageUrl) {
        this.price = Objects.requireNonNull(price, "price");
        this.imageUrl = imageUrl;
    }

    public void revise(Sku sku, Money price, int stockQuantity, String imageUrl, boolean active) {
        this.sku = Objects.requireNonNull(sku, "sku");
        reviseCommercialDetails(price, imageUrl);
        setStockQuantity(stockQuantity);
        this.active = active;
    }

    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity must not be negative");
        }
        this.stockQuantity = stockQuantity;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}
