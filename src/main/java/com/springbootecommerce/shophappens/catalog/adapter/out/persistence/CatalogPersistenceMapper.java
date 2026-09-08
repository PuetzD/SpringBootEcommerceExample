package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import com.springbootecommerce.shophappens.catalog.domain.model.CategoryId;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.ProductVariant;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CatalogPersistenceMapper {
    ProductJpaEntity toJpa(Product product, Set<CategoryJpaEntity> categories) {
        var jpa =
                ProductJpaEntity.create(
                        product.sku().value(),
                        product.name(),
                        product.description(),
                        product.price().amount(),
                        product.stockQuantity(),
                        product.imageUrl(),
                        product.active());
        product.id().ifPresent(id -> jpa.setId(id.value()));
        jpa.setCategories(categories);
        jpa.setVariants(toJpaVariants(product, jpa));
        return jpa;
    }

    void applyToJpa(ProductJpaEntity jpa, Product product, Set<CategoryJpaEntity> categories) {
        jpa.setName(product.name());
        jpa.setDescription(product.description());
        jpa.setActive(product.active());
        jpa.setCategories(new LinkedHashSet<>(categories));
        applyVariantsToJpa(jpa, product);
    }

    Product toDomain(ProductJpaEntity jpa) {
        Set<CategoryId> categoryIds =
                jpa.getCategories().stream()
                        .map(category -> new CategoryId(category.getId()))
                        .collect(Collectors.toSet());
        List<ProductVariant> variants =
                jpa.getVariants().stream()
                        .map(
                                variant ->
                                        ProductVariant.restore(
                                                new ProductVariantId(variant.getId()),
                                                new Sku(variant.getSku()),
                                                new Money(variant.getPrice()),
                                                variant.getStockQuantity(),
                                                variant.getImageUrl(),
                                                variant.isActive(),
                                                variant.isDefaultVariant()))
                        .toList();
        return Product.restore(
                new ProductId(jpa.getId()),
                jpa.getName(),
                jpa.getDescription(),
                jpa.isActive(),
                categoryIds,
                variants);
    }

    private LinkedHashSet<ProductVariantJpaEntity> toJpaVariants(
            Product product, ProductJpaEntity owner) {
        LinkedHashSet<ProductVariantJpaEntity> variants = new LinkedHashSet<>();
        product.variants()
                .forEach(
                        variant -> {
                            ProductVariantJpaEntity entity =
                                    ProductVariantJpaEntity.create(
                                            variant.sku().value(),
                                            variant.price().amount(),
                                            variant.stockQuantity(),
                                            variant.imageUrl(),
                                            variant.active(),
                                            variant.isDefault());
                            variant.id().ifPresent(id -> entity.setId(id.value()));
                            entity.setProduct(owner);
                            variants.add(entity);
                        });
        return variants;
    }

    private void applyVariantsToJpa(ProductJpaEntity jpa, Product product) {
        Map<String, ProductVariantJpaEntity> existing =
                jpa.getVariants().stream()
                        .collect(
                                Collectors.toMap(
                                        ProductVariantJpaEntity::getSku, Function.identity()));
        Map<Long, ProductVariantJpaEntity> existingById =
                jpa.getVariants().stream()
                        .collect(
                                Collectors.toMap(
                                        ProductVariantJpaEntity::getId, Function.identity()));
        LinkedHashSet<ProductVariantJpaEntity> updated = new LinkedHashSet<>();
        product.variants()
                .forEach(
                        variant -> {
                            ProductVariantJpaEntity entity =
                                    variant.id()
                                            .map(id -> existingById.get(id.value()))
                                            .or(
                                                    () ->
                                                            Optional.ofNullable(
                                                                    existing.get(
                                                                            variant.sku().value())))
                                            .orElseGet(
                                                    () ->
                                                            ProductVariantJpaEntity.create(
                                                                    variant.sku().value(),
                                                                    variant.price().amount(),
                                                                    variant.stockQuantity(),
                                                                    variant.imageUrl(),
                                                                    variant.active(),
                                                                    variant.isDefault()));
                            entity.setSku(variant.sku().value());
                            entity.setPrice(variant.price().amount());
                            entity.setStockQuantity(variant.stockQuantity());
                            entity.setImageUrl(variant.imageUrl());
                            entity.setActive(variant.active());
                            entity.setDefaultVariant(variant.isDefault());
                            entity.setProduct(jpa);
                            updated.add(entity);
                        });
        jpa.getVariants().clear();
        jpa.getVariants().addAll(updated);
    }
}
