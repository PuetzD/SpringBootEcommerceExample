package com.springbootecommerce.shophappens.catalog.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.springbootecommerce.shophappens.catalog.domain.exception.InsufficientStockException;
import com.springbootecommerce.shophappens.catalog.domain.exception.ProductUnavailableException;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    void purchasesActiveStockAndReturnsCurrentFacts() {
        Product product = productWithStock(5);

        product.purchase(2);

        assertThat(product.stockQuantity()).isEqualTo(3);
        assertThat(product.price()).isEqualTo(new Money(new BigDecimal("19.99")));
    }

    @Test
    void rejectsInactiveAndInsufficientProductsWithoutChangingStock() {
        Product inactive = productWithStock(5);
        inactive.deactivate();
        assertThatThrownBy(() -> inactive.purchase(1))
                .isInstanceOf(ProductUnavailableException.class);
        assertThat(inactive.stockQuantity()).isEqualTo(5);

        Product scarce = productWithStock(1);
        assertThatThrownBy(() -> scarce.purchase(2))
                .isInstanceOf(InsufficientStockException.class);
        assertThat(scarce.stockQuantity()).isEqualTo(1);
    }

    @Test
    void deactivationMakesProductsUnavailable() {
        Product product = productWithStock(5);

        product.deactivate();

        assertThat(product.active()).isFalse();
        assertThatThrownBy(() -> product.purchase(1))
                .isInstanceOf(ProductUnavailableException.class);
    }

    @Test
    void categoryIdsReturnsAnUnmodifiableCopy() {
        Set<CategoryId> source = new HashSet<>(Set.of(new CategoryId(3L)));
        Product product =
                Product.create(
                        new Sku("ELEC-001"),
                        "Headphones",
                        "Description",
                        new Money(new BigDecimal("19.99")),
                        5,
                        "/images/product-placeholder.svg",
                        source);

        source.add(new CategoryId(9L));

        assertThat(product.categoryIds()).containsExactly(new CategoryId(3L));
        assertThatThrownBy(() -> product.categoryIds().add(new CategoryId(7L)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private Product productWithStock(int stock) {
        return Product.create(
                new Sku("ELEC-001"), "Headphones", "Description",
                new Money(new BigDecimal("19.99")), stock,
                "/images/product-placeholder.svg", Set.of(new CategoryId(3L)));
    }
}
