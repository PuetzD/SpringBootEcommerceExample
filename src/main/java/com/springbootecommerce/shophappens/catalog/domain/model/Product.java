package com.springbootecommerce.shophappens.catalog.domain.model;

import com.springbootecommerce.shophappens.catalog.domain.exception.InsufficientStockException;
import com.springbootecommerce.shophappens.catalog.domain.exception.ProductUnavailableException;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class Product {
    private final ProductId id;
    private String name;
    private String description;
    private boolean active;
    private final Set<CategoryId> categoryIds;
    private final List<ProductVariant> variants;

    private Product(
            ProductId id,
            String name,
            String description,
            boolean active,
            Set<CategoryId> categoryIds,
            List<ProductVariant> variants) {
        this.id = id;
        this.name = Objects.requireNonNull(name).strip();
        this.description = description;
        this.active = active;
        this.categoryIds = new HashSet<>(Set.copyOf(categoryIds));
        this.variants = new ArrayList<>(List.copyOf(variants));
        validateVariants(this.variants);
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
                normalizedName,
                description,
                true,
                categoryIds,
                List.of(ProductVariant.create(sku, price, initialStock, imageUrl, true, true)));
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
                name,
                description,
                active,
                categoryIds,
                List.of(ProductVariant.create(sku, price, stockQuantity, imageUrl, active, true)));
    }

    public static Product restore(
            ProductId id,
            String name,
            String description,
            boolean active,
            Set<CategoryId> categoryIds,
            List<ProductVariant> variants) {
        return new Product(
                Objects.requireNonNull(id), name, description, active, categoryIds, variants);
    }

    public Optional<ProductId> id() {
        return Optional.ofNullable(id);
    }

    public Sku sku() {
        return defaultVariant().sku();
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Money price() {
        return defaultVariant().price();
    }

    public int stockQuantity() {
        return defaultVariant().stockQuantity();
    }

    public String imageUrl() {
        return defaultVariant().imageUrl();
    }

    public boolean active() {
        return active;
    }

    public Set<CategoryId> categoryIds() {
        return Set.copyOf(categoryIds);
    }

    public List<ProductVariant> variants() {
        return List.copyOf(variants);
    }

    public ProductVariant variant(ProductVariantId variantId) {
        return variants.stream()
                .filter(variant -> variant.id().filter(variantId::equals).isPresent())
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Variant does not belong to product"));
    }

    public void reviseVariant(
            ProductVariantId variantId,
            Sku sku,
            Money price,
            int stockQuantity,
            String imageUrl,
            boolean active) {
        ProductVariant variant = variant(variantId);
        if (variants.stream()
                .anyMatch(existing -> !existing.equals(variant) && existing.sku().equals(sku))) {
            throw new IllegalArgumentException("Variant SKU must be unique within Product");
        }
        variant.revise(sku, price, stockQuantity, imageUrl, active);
    }

    public ProductVariant defaultVariant() {
        return variants.stream().filter(ProductVariant::isDefault).findFirst().orElseThrow();
    }

    public void addVariant(ProductVariant variant) {
        Objects.requireNonNull(variant, "variant");
        if (variant.isDefault() && variants.stream().anyMatch(ProductVariant::isDefault)) {
            throw new IllegalArgumentException("Product must have exactly one default variant");
        }
        if (variants.stream().anyMatch(existing -> existing.sku().equals(variant.sku()))) {
            throw new IllegalArgumentException("Variant SKU must be unique within Product");
        }
        variants.add(variant);
    }

    public void removeVariant(ProductVariant variant) {
        Objects.requireNonNull(variant, "variant");
        if (variants.size() == 1) {
            throw new IllegalArgumentException("Product must retain at least one variant");
        }
        if (variant.isDefault()) {
            throw new IllegalArgumentException("Default variant cannot be removed");
        }
        if (!variants.remove(variant)) {
            throw new IllegalArgumentException("Variant does not belong to Product");
        }
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
        defaultVariant().reviseCommercialDetails(Objects.requireNonNull(price, "price"), imageUrl);
    }

    public void reviseFamilyDetails(String name, String description) {
        this.name = normalizeName(name);
        this.description = description;
    }

    public void replaceCategories(Set<CategoryId> categoryIds) {
        this.categoryIds.clear();
        this.categoryIds.addAll(Set.copyOf(categoryIds));
    }

    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity must not be negative");
        }
        defaultVariant().setStockQuantity(stockQuantity);
    }

    public PurchasedFacts purchase(int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("Quantity must be positive");
        ProductVariant variant = defaultVariant();
        if (variant.id().isPresent()) {
            return purchase(variant.id().orElseThrow(), quantity);
        }
        if (!active || !variant.active()) throw new ProductUnavailableException(id, variant.sku());
        if (variant.stockQuantity() < quantity) {
            throw new InsufficientStockException(
                    id, variant.sku(), quantity, variant.stockQuantity());
        }
        variant.setStockQuantity(variant.stockQuantity() - quantity);
        return new PurchasedFacts(
                id == null ? null : new ProductVariantId(id.value()),
                id,
                variant.sku(),
                name,
                variant.price(),
                quantity);
    }

    public PurchasedFacts purchase(ProductVariantId variantId, int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("Quantity must be positive");
        ProductVariant variant =
                variants.stream()
                        .filter(candidate -> candidate.id().filter(variantId::equals).isPresent())
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Variant does not belong to product"));
        if (!active || !variant.active()) throw new ProductUnavailableException(id, variant.sku());
        if (variant.stockQuantity() < quantity) {
            throw new InsufficientStockException(
                    id, variant.sku(), quantity, variant.stockQuantity());
        }
        variant.setStockQuantity(variant.stockQuantity() - quantity);
        return new PurchasedFacts(
                variant.id().orElseThrow(), id, variant.sku(), name, variant.price(), quantity);
    }

    private static String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        return name.strip();
    }

    private static void validateVariants(List<ProductVariant> variants) {
        if (variants.isEmpty()) {
            throw new IllegalArgumentException("Product must have at least one variant");
        }
        if (variants.stream().filter(ProductVariant::isDefault).count() != 1) {
            throw new IllegalArgumentException("Product must have exactly one default variant");
        }
    }
}
