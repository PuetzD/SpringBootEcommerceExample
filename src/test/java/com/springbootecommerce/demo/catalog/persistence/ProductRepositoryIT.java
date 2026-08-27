package com.springbootecommerce.demo.catalog.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.demo.catalog.domain.Category;
import com.springbootecommerce.demo.catalog.domain.Product;
import com.springbootecommerce.demo.integration.AbstractIntegrationTest;
import com.springbootecommerce.demo.sharedkernel.money.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProductRepositoryIT extends AbstractIntegrationTest {

    @Autowired ProductRepository productRepository;
    @Autowired CategoryRepository categoryRepository;

    @Test
    void findsProductBySku() {
        var product =
                Product.create(
                        "SHOE-001",
                        "Running Shoes",
                        "Comfortable running shoes",
                        new Money(new BigDecimal("99.99")),
                        10,
                        "/images/product-placeholder.svg");
        productRepository.saveAndFlush(product);
        var found = productRepository.findBySku("SHOE-001");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Running Shoes");
        assertThat(found.get().getPrice().amount()).isEqualByComparingTo("99.99");
    }

    @Test
    void findsActiveProductsByCategory() {
        var category = new Category();
        category.setName("Test Electronics");
        category.setSlug("test-electronics");
        categoryRepository.saveAndFlush(category);

        var activeProduct =
                Product.create(
                        "TEST-ELEC-001",
                        "Test Headphones",
                        "Over-ear headphones",
                        new Money(new BigDecimal("49.99")),
                        5,
                        "/images/product-placeholder.svg");
        activeProduct.getCategories().add(category);
        productRepository.saveAndFlush(activeProduct);

        var inactiveProduct =
                Product.create(
                        "TEST-ELEC-002",
                        "Test Old Speaker",
                        "An old speaker",
                        new Money(new BigDecimal("29.99")),
                        0,
                        "/images/product-placeholder.svg");
        inactiveProduct.deactivate();
        inactiveProduct.getCategories().add(category);
        productRepository.saveAndFlush(inactiveProduct);

        var results = productRepository.findByActiveTrueAndCategoriesId(category.getId());
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getSku()).isEqualTo("TEST-ELEC-001");
    }

    @Test
    void returnsEmptyForMissingSku() {
        assertThat(productRepository.findBySku("NONEXISTENT")).isEmpty();
    }
}
