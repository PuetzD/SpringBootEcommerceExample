package com.springbootecommerce.shophappens.ordering.adapter.out.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.PublishedInsufficientStockException;
import com.springbootecommerce.shophappens.catalog.application.port.in.PublishedProductUnavailableException;
import com.springbootecommerce.shophappens.catalog.application.port.in.PurchaseLine;
import com.springbootecommerce.shophappens.catalog.application.port.in.PurchaseProductsUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.PurchasedProductSnapshot;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.ordering.application.exception.CheckoutItemUnavailableException;
import com.springbootecommerce.shophappens.ordering.application.port.out.PurchasedProduct;
import com.springbootecommerce.shophappens.ordering.application.port.out.RequestedProduct;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CatalogPurchaseGatewayAdapterTest {
    @Mock PurchaseProductsUseCase purchaseProducts;
    CatalogPurchaseGatewayAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CatalogPurchaseGatewayAdapter(purchaseProducts);
    }

    @Test
    void translatesMissingProductAndPreservesCause() {
        var cause = new PublishedProductUnavailableException(new ProductId(7L), null);
        when(purchaseProducts.purchase(List.of(line(7L, 1)))).thenThrow(cause);

        Throwable thrown = catchThrowable(() -> adapter.purchase(List.of(requested(7L, 1))));

        assertThat(thrown).isInstanceOf(CheckoutItemUnavailableException.class);
        assertThat(thrown.getCause()).isSameAs(cause);
    }

    @Test
    void translatesInsufficientStockAndPreservesCause() {
        var cause =
                new PublishedInsufficientStockException(
                        new ProductId(7L), new Sku("WEAP-002"), 3, 1);
        when(purchaseProducts.purchase(List.of(line(7L, 3)))).thenThrow(cause);

        Throwable thrown = catchThrowable(() -> adapter.purchase(List.of(requested(7L, 3))));

        assertThat(thrown).isInstanceOf(CheckoutItemUnavailableException.class);
        assertThat(thrown.getCause()).isSameAs(cause);
    }

    @Test
    void mapsSuccessfulSnapshotWithoutChangingPurchaseFacts() {
        var snapshot =
                new PurchasedProductSnapshot(
                        new ProductReference(7L),
                        "WEAP-002",
                        "Rubber Duck of Debugging",
                        new Money(new BigDecimal("18.99")),
                        2);
        when(purchaseProducts.purchase(List.of(line(7L, 2)))).thenReturn(List.of(snapshot));

        assertThat(adapter.purchase(List.of(requested(7L, 2))))
                .containsExactly(
                        new PurchasedProduct(
                                new ProductId(7L),
                                "WEAP-002",
                                "Rubber Duck of Debugging",
                                new Money(new BigDecimal("18.99")),
                                2));
    }

    private static RequestedProduct requested(long productId, int quantity) {
        return new RequestedProduct(new ProductId(productId), quantity);
    }

    private static PurchaseLine line(long productId, int quantity) {
        return new PurchaseLine(new ProductReference(productId), quantity);
    }
}
