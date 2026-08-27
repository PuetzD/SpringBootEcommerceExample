package com.springbootecommerce.demo.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.springbootecommerce.demo.catalog.domain.Product;
import com.springbootecommerce.demo.catalog.domain.ProductUnavailableException;
import com.springbootecommerce.demo.catalog.persistence.ProductRepository;
import com.springbootecommerce.demo.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class CatalogPurchaseServiceTest {

    private ProductRepository productRepository;
    private CatalogPurchaseService service;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        service = new CatalogPurchaseService(productRepository);
    }

    @Test
    void reservesProductsAndReturnsImmutablePurchaseFacts() {
        var product = activeProduct("ELEC-001", "Headphones", "149.99", 5);
        when(productRepository.findById(7L)).thenReturn(Optional.of(product));

        var purchased = service.reserve(List.of(new PurchaseRequest(7L, 2)));

        assertThat(purchased)
                .containsExactly(
                        new PurchasedProduct(
                                7L,
                                "ELEC-001",
                                "Headphones",
                                new Money(new BigDecimal("149.99")),
                                2));
        assertThat(product.getStockQuantity()).isEqualTo(3);
    }

    @Test
    void rejectsAProductMissingFromCatalog() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.reserve(List.of(new PurchaseRequest(99L, 1))))
                .isInstanceOf(ProductUnavailableException.class);
    }

    @Test
    void sortsRequestsByProductIdAndLoadsInDeterministicOrder() {
        var product7 = activeProduct("ELEC-007", "Keyboard", "79.99", 5);
        var product8 = activeProduct("ELEC-008", "Mouse", "29.99", 5);
        when(productRepository.findById(7L)).thenReturn(Optional.of(product7));
        when(productRepository.findById(8L)).thenReturn(Optional.of(product8));

        var purchased =
                service.reserve(List.of(new PurchaseRequest(8L, 1), new PurchaseRequest(7L, 2)));

        InOrder inOrder = inOrder(productRepository);
        inOrder.verify(productRepository).findById(7L);
        inOrder.verify(productRepository).findById(8L);

        assertThat(purchased).extracting(PurchasedProduct::productId).containsExactly(7L, 8L);
    }

    private Product activeProduct(String sku, String name, String price, int stock) {
        return Product.create(
                sku,
                name,
                name + " description",
                new Money(new BigDecimal(price)),
                stock,
                "/images/product-placeholder.svg");
    }
}
