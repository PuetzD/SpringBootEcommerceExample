package com.springbootecommerce.shophappens.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.catalog.domain.Product;
import com.springbootecommerce.shophappens.catalog.persistence.ProductRepository;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CatalogQueryServiceTest {

    private ProductRepository productRepository;
    private CatalogQueryService service;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        service = new CatalogQueryService(productRepository);
    }

    @Test
    void returnsActiveProductsAsImmutableSummaries() {
        var product = activeProduct("TEST-001", "Test Product", "99.99");
        when(productRepository.findByActiveTrueOrderByNameAscIdAsc()).thenReturn(List.of(product));

        var results = service.findAllActiveProducts();

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().name()).isEqualTo("Test Product");
    }

    @Test
    void returnsEmptyListWhenNoActiveProducts() {
        when(productRepository.findByActiveTrueOrderByNameAscIdAsc()).thenReturn(List.of());

        assertThat(service.findAllActiveProducts()).isEmpty();
    }

    @Test
    void returnsActiveProductById() {
        var product = activeProduct("ELEC-001", "Headphones", "149.99");
        when(productRepository.findByIdAndActiveTrue(7L)).thenReturn(Optional.of(product));

        assertThat(service.findActiveProductById(7L))
                .get()
                .extracting(ProductSummary::name)
                .isEqualTo("Headphones");
    }

    @Test
    void doesNotReturnInactiveProductById() {
        when(productRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThat(service.findActiveProductById(99L)).isEmpty();
    }

    @Test
    void returnsActiveProductBySku() {
        var product = activeProduct("SHOE-001", "Running Shoes", "89.99");
        when(productRepository.findBySkuAndActiveTrue("SHOE-001")).thenReturn(Optional.of(product));

        assertThat(service.findActiveProductBySku("SHOE-001"))
                .get()
                .extracting(ProductSummary::name)
                .isEqualTo("Running Shoes");
    }

    @Test
    void doesNotReturnInactiveProductBySku() {
        when(productRepository.findBySkuAndActiveTrue("OLD-001")).thenReturn(Optional.empty());

        assertThat(service.findActiveProductBySku("OLD-001")).isEmpty();
    }

    private Product activeProduct(String sku, String name, String price) {
        return Product.create(
                sku,
                name,
                "Description for " + name,
                new Money(new BigDecimal(price)),
                10,
                "/images/product-placeholder.svg");
    }
}
