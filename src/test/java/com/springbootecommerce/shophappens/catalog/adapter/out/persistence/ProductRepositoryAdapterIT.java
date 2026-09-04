package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.domain.model.CategoryId;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class ProductRepositoryAdapterIT extends AbstractIntegrationTest {
    @Autowired ProductRepository products;
    @Autowired JdbcTemplate jdbc;

    @Test
    void restoresMoneyCategoriesAndOptimisticVersion() {
        seedProduct("FIX-001", "Wireless Headphones", new Money(new BigDecimal("149.99")), 25);

        Product product = products.findActiveBySku(new Sku("FIX-001")).orElseThrow();

        assertThat(product.price()).isEqualTo(new Money(new BigDecimal("149.99")));
        assertThat(product.categoryIds()).isNotEmpty();
        assertThat(product.id()).isPresent();
    }

    @Test
    void persistsPermanentStockDecrease() {
        seedProduct("FIX-002", "Smart Watch", new Money(new BigDecimal("199.99")), 15);
        Product product = products.findActiveBySku(new Sku("FIX-002")).orElseThrow();
        int before = product.stockQuantity();
        product.purchase(1);

        products.save(product);

        assertThat(products.findById(product.id().orElseThrow()).orElseThrow().stockQuantity())
                .isEqualTo(before - 1);
    }

    @Test
    void findForPurchaseLocksAndRestoresDetailedAggregateWithinAdapterTransaction() {
        Product seeded =
                seedProduct("FIX-LOCK", "Concurrency Staff", new Money(new BigDecimal("79.99")), 1);

        Product product = products.findForPurchase(seeded.id().orElseThrow()).orElseThrow();

        assertThat(product.id()).contains(new ProductId(seeded.id().orElseThrow().value()));
        assertThat(product.categoryIds()).containsExactlyElementsOf(seeded.categoryIds());
        assertThat(product.stockQuantity()).isOne();
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

    @Test
    void returnsStableBoundedActivePage() {
        seedProduct("PAGE-A", "Alpha", "1.00", 5);
        seedProduct("PAGE-B", "Bravo", "1.00", 5);
        seedProduct("PAGE-C", "Charlie", "1.00", 5);

        var result = products.findActivePage(1, 2);

        assertThat(result.products()).extracting(Product::name).containsExactly("Charlie");
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(3);
        assertThat(result.totalPages()).isEqualTo(2);
    }

    private static Comparator<Product> orderByNameThenId() {
        return Comparator.comparing(Product::name)
                .thenComparing(product -> product.id().orElseThrow().value());
    }

    private Product seedProduct(String sku, String name, Money price, int stock) {
        Long categoryId =
                jdbc.queryForObject(
                        "insert into category (name, slug) values (?, ?) returning id",
                        Long.class,
                        name + " category",
                        "cat-" + sku.toLowerCase());
        Product product =
                Product.create(
                        new Sku(sku),
                        name,
                        "Fixture product",
                        price,
                        stock,
                        "/images/product-placeholder.svg",
                        Set.of(new CategoryId(categoryId)));
        return products.save(product);
    }

    private Product seedProduct(String sku, String name, String price, int stock) {
        return seedProduct(sku, name, new Money(new BigDecimal(price)), stock);
    }
}
