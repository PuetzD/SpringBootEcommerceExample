package com.springbootecommerce.shophappens.catalog.application.service;

import com.springbootecommerce.shophappens.catalog.application.port.in.BrowseCatalogUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductSummary;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.ProductId;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

// @Service intentionally deferred to Task 4 (web adapter move) to avoid a duplicate-bean-name
// collision with the legacy catalog.application service; legacy layer deleted in Task 4.
@Transactional(readOnly = true)
public class CatalogQueryService implements BrowseCatalogUseCase {
    private final ProductRepository productRepository;

    public CatalogQueryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductSummary> findAllActive() {
        return productRepository.findAllActive().stream().map(this::toSummary).toList();
    }

    @Override
    public Optional<ProductSummary> findActiveById(ProductReference product) {
        return productRepository
                .findActiveById(new ProductId(product.value()))
                .map(this::toSummary);
    }

    @Override
    public Optional<ProductSummary> findActiveBySku(String sku) {
        return productRepository.findActiveBySku(new Sku(sku)).map(this::toSummary);
    }

    private ProductSummary toSummary(Product product) {
        return new ProductSummary(
                new ProductReference(product.id().get().value()),
                product.sku().value(),
                product.name(),
                product.description(),
                product.price(),
                product.stockQuantity(),
                product.imageUrl());
    }
}
