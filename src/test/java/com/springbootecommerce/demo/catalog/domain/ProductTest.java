package com.springbootecommerce.demo.catalog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.springbootecommerce.demo.sharedkernel.money.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    void reservesStockForAnActiveProduct() {
        var product = activeProductWithStock(5);
        product.reserveStock(2);
        assertThat(product.getStockQuantity()).isEqualTo(3);
    }

    @Test
    void rejectsInactiveProductAndInsufficientStock() {
        var inactive = activeProductWithStock(5);
        inactive.deactivate();
        assertThatThrownBy(() -> inactive.reserveStock(1))
                .isInstanceOf(ProductUnavailableException.class);

        var scarce = activeProductWithStock(1);
        assertThatThrownBy(() -> scarce.reserveStock(2))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void rejectsNonPositiveReservation() {
        assertThatThrownBy(() -> activeProductWithStock(5).reserveStock(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void creationRejectsNegativeStockAndCommercialStateHasNoPublicSetters() {
        assertThatThrownBy(() -> activeProductWithStock(-1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(Product.class.getMethods())
                .extracting(java.lang.reflect.Method::getName)
                .doesNotContain(
                        "setSku",
                        "setName",
                        "setDescription",
                        "setPrice",
                        "setStockQuantity",
                        "setImageUrl",
                        "setActive");
    }

    private Product activeProductWithStock(int stock) {
        return Product.create(
                "TEST-001",
                "Test Product",
                "Test description",
                new Money(new BigDecimal("10.00")),
                stock,
                "/images/product-placeholder.svg");
    }
}
