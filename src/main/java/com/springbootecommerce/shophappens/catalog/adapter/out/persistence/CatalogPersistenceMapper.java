package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import com.springbootecommerce.shophappens.catalog.domain.model.CategoryId;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.ProductId;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
        return jpa;
    }

    void applyToJpa(ProductJpaEntity jpa, Product product, Set<CategoryJpaEntity> categories) {
        jpa.setSku(product.sku().value());
        jpa.setName(product.name());
        jpa.setDescription(product.description());
        jpa.setPrice(product.price().amount());
        jpa.setStockQuantity(product.stockQuantity());
        jpa.setImageUrl(product.imageUrl());
        jpa.setActive(product.active());
        jpa.setCategories(new LinkedHashSet<>(categories));
    }

    Product toDomain(ProductJpaEntity jpa) {
        Set<CategoryId> categoryIds =
                jpa.getCategories().stream()
                        .map(category -> new CategoryId(category.getId()))
                        .collect(Collectors.toSet());
        return Product.restore(
                new ProductId(jpa.getId()),
                new Sku(jpa.getSku()),
                jpa.getName(),
                jpa.getDescription(),
                new Money(jpa.getPrice()),
                jpa.getStockQuantity(),
                jpa.getImageUrl(),
                jpa.isActive(),
                categoryIds);
    }
}
