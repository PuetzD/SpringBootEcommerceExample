package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
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
}
