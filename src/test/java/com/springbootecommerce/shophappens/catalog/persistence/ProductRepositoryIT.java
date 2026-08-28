package com.springbootecommerce.shophappens.catalog.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.shophappens.catalog.domain.Product;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProductRepositoryIT extends AbstractIntegrationTest {

    @Autowired ProductRepository productRepository;

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
    void ordersActiveProductsByNameThenId() {
        var banana =
                Product.create(
                        "FRUIT-001",
                        "Banana",
                        "Yellow fruit",
                        new Money(new BigDecimal("0.50")),
                        20,
                        "/images/product-placeholder.svg");
        var apple =
                Product.create(
                        "FRUIT-002",
                        "Apple",
                        "Red fruit",
                        new Money(new BigDecimal("0.80")),
                        15,
                        "/images/product-placeholder.svg");
        var secondBanana =
                Product.create(
                        "FRUIT-003",
                        "Banana",
                        "Another banana",
                        new Money(new BigDecimal("0.60")),
                        5,
                        "/images/product-placeholder.svg");
        var inactive =
                Product.create(
                        "FRUIT-004",
                        "AAA Inactive",
                        "Should not appear",
                        new Money(new BigDecimal("1.00")),
                        0,
                        "/images/product-placeholder.svg");
        inactive.deactivate();
        productRepository.saveAllAndFlush(java.util.List.of(banana, apple, secondBanana, inactive));

        var results = productRepository.findByActiveTrueOrderByNameAscIdAsc();

        assertThat(results)
                .extracting(Product::getSku)
                .containsExactly("FRUIT-002", "FRUIT-001", "FRUIT-003");
    }

    @Test
    void returnsEmptyForMissingSku() {
        assertThat(productRepository.findBySku("NONEXISTENT")).isEmpty();
    }
}
