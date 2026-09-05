package com.springbootecommerce.shophappens.catalog.application.service;

import com.springbootecommerce.shophappens.catalog.application.port.in.BrowseCatalogUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.CatalogPage;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductSummary;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CatalogQueryService implements BrowseCatalogUseCase {
    private static final int MAX_PAGE_SIZE = 20;
    private final ProductRepository productRepository;

    @Override
    public CatalogPage findActivePage(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must not be negative");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must be between 1 and 20");
        }
        var result = productRepository.findActivePage(page, size);
        return new CatalogPage(
                result.products().stream().map(this::toSummary).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
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
