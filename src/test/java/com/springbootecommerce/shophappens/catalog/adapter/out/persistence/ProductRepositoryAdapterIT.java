package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProductRepositoryAdapterIT extends AbstractIntegrationTest {
    @Autowired ProductRepository products;

    @Test
    void restoresMoneyCategoriesAndOptimisticVersion() {
        Product product = products.findActiveBySku(new Sku("ELEC-001")).orElseThrow();

        assertThat(product.price()).isEqualTo(new Money(new BigDecimal("149.99")));
        assertThat(product.categoryIds()).isNotEmpty();
        assertThat(product.id()).isPresent();
    }

    @Test
    void persistsPermanentStockDecrease() {
        Product product = products.findActiveBySku(new Sku("ELEC-001")).orElseThrow();
        int before = product.stockQuantity();
        product.purchase(1);

        products.save(product);

        assertThat(products.findById(product.id().orElseThrow()).orElseThrow().stockQuantity())
                .isEqualTo(before - 1);
    }

    @Test
    void ordersActiveProductsByNameThenId() {
        Product banana =
                Product.create(
                        new Sku("ORD-A"),
                        "Banana",
                        "Yellow fruit",
                        new Money(new BigDecimal("0.50")),
                        20,
                        "/images/product-placeholder.svg",
                        Set.of());
        Product apple =
                Product.create(
                        new Sku("ORD-B"),
                        "Apple",
                        "Red fruit",
                        new Money(new BigDecimal("0.80")),
                        15,
                        "/images/product-placeholder.svg",
                        Set.of());
        Product secondBanana =
                Product.create(
                        new Sku("ORD-C"),
                        "Banana",
                        "Another banana",
                        new Money(new BigDecimal("0.60")),
                        5,
                        "/images/product-placeholder.svg",
                        Set.of());
        Product inactive =
                Product.create(
                        new Sku("ORD-D"),
                        "AAA Inactive",
                        "Should not appear",
                        new Money(new BigDecimal("1.00")),
                        0,
                        "/images/product-placeholder.svg",
                        Set.of());
        inactive.deactivate();
        products.save(banana);
        products.save(apple);
        products.save(secondBanana);
        products.save(inactive);

        List<Product> results = products.findAllActive();

        assertThat(results).isSortedAccordingTo(orderByNameThenId());
        assertThat(
                        results.stream()
                                .map(product -> product.sku().value())
                                .filter(sku -> sku.startsWith("ORD-")))
                .containsExactly("ORD-B", "ORD-A", "ORD-C");
    }

    private static Comparator<Product> orderByNameThenId() {
        return Comparator.comparing(Product::name)
                .thenComparing(product -> product.id().orElseThrow().value());
    }
}
