package com.springbootecommerce.demo.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.springbootecommerce.demo.catalog.domain.Category;
import com.springbootecommerce.demo.catalog.domain.Product;
import com.springbootecommerce.demo.catalog.persistence.CategoryRepository;
import com.springbootecommerce.demo.catalog.persistence.ProductRepository;
import com.springbootecommerce.demo.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CatalogQueryServiceTest {

    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private CatalogQueryService catalogQueryService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        catalogQueryService = new CatalogQueryService(productRepository, categoryRepository);
    }

    @Test
    void returnsActiveProducts() {
        var category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        category.setSlug("electronics");

        var product =
                Product.create(
                        "TEST-001",
                        "Test Product",
                        "Test description",
                        new Money(new BigDecimal("99.99")),
                        10,
                        "/images/product-placeholder.svg");
        product.getCategories().add(category);

        when(productRepository.findByActiveTrue()).thenReturn(List.of(product));

        var results = catalogQueryService.findAllActiveProducts();
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().name()).isEqualTo("Test Product");
    }

    @Test
    void returnsEmptyListWhenNoActiveProducts() {
        when(productRepository.findByActiveTrue()).thenReturn(List.of());

        var results = catalogQueryService.findAllActiveProducts();
        assertThat(results).isEmpty();
    }

    @Test
    void findsProductBySku() {
        var category = new Category();
        category.setId(1L);
        category.setName("Shoes");
        category.setSlug("shoes");

        var product =
                Product.create(
                        "SHOE-001",
                        "Running Shoes",
                        "Comfortable running shoes",
                        new Money(new BigDecimal("89.99")),
                        10,
                        "/images/product-placeholder.svg");
        product.getCategories().add(category);

        when(productRepository.findBySku("SHOE-001")).thenReturn(java.util.Optional.of(product));

        var found = catalogQueryService.findProductBySku("SHOE-001");
        assertThat(found).isPresent();
        assertThat(found.get().name()).isEqualTo("Running Shoes");
        assertThat(found.get().sku()).isEqualTo("SHOE-001");
    }

    @Test
    void returnsAllCategories() {
        var category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        category.setSlug("electronics");

        when(categoryRepository.findAll()).thenReturn(List.of(category));

        var results = catalogQueryService.findAllCategories();
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().name()).isEqualTo("Electronics");
    }
}
