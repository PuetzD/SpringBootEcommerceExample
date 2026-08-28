package com.springbootecommerce.shophappens.catalog.application;

import com.springbootecommerce.shophappens.catalog.domain.Product;
import com.springbootecommerce.shophappens.catalog.persistence.ProductRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CatalogQueryService {

    private final ProductRepository productRepository;

    public CatalogQueryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductSummary> findAllActiveProducts() {
        return productRepository.findByActiveTrueOrderByNameAscIdAsc().stream()
                .map(this::toSummary)
                .toList();
    }

    public Optional<ProductSummary> findActiveProductById(Long id) {
        return productRepository.findByIdAndActiveTrue(id).map(this::toSummary);
    }

    public Optional<ProductSummary> findActiveProductBySku(String sku) {
        return productRepository.findBySkuAndActiveTrue(sku).map(this::toSummary);
    }

    private ProductSummary toSummary(Product product) {
        return new ProductSummary(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getImageUrl());
    }
}
