package com.springbootecommerce.shophappens.catalog.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.PublishedProductUnavailableException;
import com.springbootecommerce.shophappens.catalog.application.port.in.PurchaseLine;
import com.springbootecommerce.shophappens.catalog.application.port.in.PurchasedProductSnapshot;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.domain.exception.InsufficientStockException;
import com.springbootecommerce.shophappens.catalog.domain.exception.ProductUnavailableException;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CatalogPurchaseServiceTest {
    @Mock ProductRepository products;
    CatalogPurchaseService service;

    @BeforeEach
    void setUp() {
        service = new CatalogPurchaseService(products);
    }

    @Test
    void purchasesInStableProductOrderAndReturnsSnapshots() {
        Product seven = restoredProduct(7L, "WEAP-002", "Rubber Duck of Debugging", "18.99", 5);
        Product eight =
                restoredProduct(8L, "MAGI-006", "Staff of Dependency Injection", "89.99", 5);
        when(products.findForPurchase(new ProductId(7L))).thenReturn(Optional.of(seven));
        when(products.findForPurchase(new ProductId(8L))).thenReturn(Optional.of(eight));

        List<PurchasedProductSnapshot> result =
                service.purchase(
                        List.of(
                                new PurchaseLine(new ProductReference(8L), 1),
                                new PurchaseLine(new ProductReference(7L), 2)));

        assertThat(result)
                .extracting(snapshot -> snapshot.product().value())
                .containsExactly(7L, 8L);
        InOrder order = inOrder(products);
        order.verify(products).findForPurchase(new ProductId(7L));
        order.verify(products).findForPurchase(new ProductId(8L));
        verify(products, never()).findById(any(ProductId.class));
    }

    @Test
    void purchaseReturnsSnapshotsWithSkuNameUnitPriceQuantityAndLineTotal() {
        Product product = restoredProduct(7L, "WEAP-002", "Rubber Duck of Debugging", "18.99", 10);
        when(products.findForPurchase(new ProductId(7L))).thenReturn(Optional.of(product));

        List<PurchasedProductSnapshot> result =
                service.purchase(List.of(new PurchaseLine(new ProductReference(7L), 3)));

        assertThat(result).hasSize(1);
        PurchasedProductSnapshot snapshot = result.get(0);
        assertThat(snapshot.product().value()).isEqualTo(7L);
        assertThat(snapshot.sku()).isEqualTo("WEAP-002");
        assertThat(snapshot.name()).isEqualTo("Rubber Duck of Debugging");
        assertThat(snapshot.unitPrice()).isEqualTo(new Money(new BigDecimal("18.99")));
        assertThat(snapshot.quantity()).isEqualTo(3);
        assertThat(snapshot.lineTotal()).isEqualTo(new Money(new BigDecimal("56.97")));
    }

    @Test
    void purchaseThrowsProductUnavailableWhenProductMissing() {
        when(products.findForPurchase(new ProductId(7L))).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                service.purchase(
                                        List.of(new PurchaseLine(new ProductReference(7L), 1))))
                .isInstanceOf(ProductUnavailableException.class);
        verify(products, never()).save(any());
    }

    @Test
    void purchaseThrowsPublishedProductUnavailableWhenProductInactive() {
        Product product = restoredProduct(7L, "WEAP-002", "Rubber Duck of Debugging", "18.99", 5);
        product.deactivate();
        when(products.findForPurchase(new ProductId(7L))).thenReturn(Optional.of(product));

        assertThatThrownBy(
                        () ->
                                service.purchase(
                                        List.of(new PurchaseLine(new ProductReference(7L), 1))))
                .isInstanceOf(PublishedProductUnavailableException.class);
        verify(products, never()).save(any());
    }

    @Test
    void purchaseThrowsInsufficientStockWhenStockTooLow() {
        Product product = restoredProduct(7L, "WEAP-002", "Rubber Duck of Debugging", "18.99", 1);
        when(products.findForPurchase(new ProductId(7L))).thenReturn(Optional.of(product));

        assertThatThrownBy(
                        () ->
                                service.purchase(
                                        List.of(new PurchaseLine(new ProductReference(7L), 5))))
                .isInstanceOf(InsufficientStockException.class);
        verify(products, never()).save(any());
    }

    @Test
    void purchaseRejectsDuplicateProductReferencesWithoutLoadingOrSavingTwice() {
        assertThatThrownBy(
                        () ->
                                service.purchase(
                                        List.of(
                                                new PurchaseLine(new ProductReference(7L), 1),
                                                new PurchaseLine(new ProductReference(7L), 2))))
                .isInstanceOf(IllegalArgumentException.class);
        verify(products, never()).findForPurchase(any(ProductId.class));
        verify(products, never()).findById(any(ProductId.class));
        verify(products, never()).save(any());
    }

    private Product restoredProduct(long id, String sku, String name, String price, int stock) {
        return Product.restore(
                new ProductId(id),
                new Sku(sku),
                name,
                "Description",
                new Money(new BigDecimal(price)),
                stock,
                "/images/product-placeholder.svg",
                true,
                Set.of());
    }
}
