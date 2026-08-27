package com.springbootecommerce.demo.catalog.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.demo.catalog.domain.Category;
import com.springbootecommerce.demo.catalog.domain.Product;
import com.springbootecommerce.demo.integration.AbstractIntegrationTest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProductRepositoryIT extends AbstractIntegrationTest {

    @Autowired ProductRepository productRepository;
    @Autowired CategoryRepository categoryRepository;

    @Test
    void findsProductBySku() {
        var product = new Product();
        product.setSku("SHOE-001");
        product.setName("Running Shoes");
        product.setPrice(new BigDecimal("99.99"));
        product.setStockQuantity(10);
        product.setActive(true);
        productRepository.saveAndFlush(product);
        var found = productRepository.findBySku("SHOE-001");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Running Shoes");
    }

    @Test
    void findsActiveProductsByCategory() {
        var category = new Category();
        category.setName("Test Electronics");
        category.setSlug("test-electronics");
        categoryRepository.saveAndFlush(category);

        var activeProduct = new Product();
        activeProduct.setSku("TEST-ELEC-001");
        activeProduct.setName("Test Headphones");
        activeProduct.setPrice(new BigDecimal("49.99"));
        activeProduct.setStockQuantity(5);
        activeProduct.setActive(true);
        activeProduct.getCategories().add(category);
        productRepository.saveAndFlush(activeProduct);

        var inactiveProduct = new Product();
        inactiveProduct.setSku("TEST-ELEC-002");
        inactiveProduct.setName("Test Old Speaker");
        inactiveProduct.setPrice(new BigDecimal("29.99"));
        inactiveProduct.setStockQuantity(0);
        inactiveProduct.setActive(false);
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
