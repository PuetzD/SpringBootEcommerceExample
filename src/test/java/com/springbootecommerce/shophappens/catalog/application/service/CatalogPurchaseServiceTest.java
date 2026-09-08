package com.springbootecommerce.shophappens.catalog.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.PublishedInsufficientStockException;
import com.springbootecommerce.shophappens.catalog.application.port.in.PublishedProductUnavailableException;
import com.springbootecommerce.shophappens.catalog.application.port.in.PurchaseLine;
import com.springbootecommerce.shophappens.catalog.application.port.in.PurchasedProductSnapshot;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.ProductVariant;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.List;
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
        when(products.findAllForPurchase(
                        List.of(new ProductVariantId(7L), new ProductVariantId(8L))))
                .thenReturn(List.of(seven, eight));

        List<PurchasedProductSnapshot> result =
                service.purchase(
                        List.of(
                                new PurchaseLine(new ProductReference(8L), 1),
                                new PurchaseLine(new ProductReference(7L), 2)));

        assertThat(result)
                .extracting(snapshot -> snapshot.product().value())
                .containsExactly(7L, 8L);
        InOrder order = inOrder(products);
        order.verify(products)
                .findAllForPurchase(List.of(new ProductVariantId(7L), new ProductVariantId(8L)));
        order.verify(products).save(seven);
        order.verify(products).save(eight);
        verify(products, never()).findById(any(ProductId.class));
    }

    @Test
    void purchaseReturnsSnapshotsWithSkuNameUnitPriceQuantityAndLineTotal() {
        Product product = restoredProduct(7L, "WEAP-002", "Rubber Duck of Debugging", "18.99", 10);
        when(products.findAllForPurchase(List.of(new ProductVariantId(7L))))
                .thenReturn(List.of(product));

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
        when(products.findAllForPurchase(List.of(new ProductVariantId(7L))))
                .thenThrow(new PublishedProductUnavailableException(null, null));

        assertThatThrownBy(
                        () ->
                                service.purchase(
                                        List.of(new PurchaseLine(new ProductReference(7L), 1))))
                .isInstanceOf(PublishedProductUnavailableException.class);
        verify(products, never()).save(any());
    }

    @Test
    void purchaseThrowsPublishedProductUnavailableWhenProductInactive() {
        Product product = restoredProduct(7L, "WEAP-002", "Rubber Duck of Debugging", "18.99", 5);
        product.deactivate();
        when(products.findAllForPurchase(List.of(new ProductVariantId(7L))))
                .thenReturn(List.of(product));

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
        when(products.findAllForPurchase(List.of(new ProductVariantId(7L))))
                .thenReturn(List.of(product));

        assertThatThrownBy(
                        () ->
                                service.purchase(
                                        List.of(new PurchaseLine(new ProductReference(7L), 5))))
                .isInstanceOf(PublishedInsufficientStockException.class);
        verify(products, never()).save(any());
    }

    @Test
    void validatesEveryLineBeforeSavingAnyFamily() {
        Product seven = restoredProduct(7L, "WEAP-002", "Rubber Duck of Debugging", "18.99", 5);
        Product eight =
                restoredProduct(8L, "MAGI-006", "Staff of Dependency Injection", "89.99", 0);
        when(products.findAllForPurchase(
                        List.of(new ProductVariantId(7L), new ProductVariantId(8L))))
                .thenReturn(List.of(seven, eight));

        assertThatThrownBy(
                        () ->
                                service.purchase(
                                        List.of(
                                                new PurchaseLine(new ProductReference(7L), 1),
                                                new PurchaseLine(new ProductReference(8L), 1))))
                .isInstanceOf(PublishedInsufficientStockException.class);

        verify(products, never()).save(any());
    }

    @Test
    void savesOneFamilyOnceWhenPurchasingTwoSiblingVariants() {
        Product family =
                Product.restore(
                        new ProductId(70L),
                        "Shirt",
                        "Description",
                        true,
                        Set.of(),
                        List.of(
                                ProductVariant.restore(
                                        new ProductVariantId(7L),
                                        new Sku("SHIRT-S"),
                                        new Money(new BigDecimal("18.99")),
                                        5,
                                        null,
                                        true,
                                        true),
                                ProductVariant.restore(
                                        new ProductVariantId(8L),
                                        new Sku("SHIRT-L"),
                                        new Money(new BigDecimal("20.99")),
                                        5,
                                        null,
                                        true,
                                        false)));
        when(products.findAllForPurchase(
                        List.of(new ProductVariantId(7L), new ProductVariantId(8L))))
                .thenReturn(List.of(family));

        service.purchase(
                List.of(
                        new PurchaseLine(new ProductVariantId(8L), 1),
                        new PurchaseLine(new ProductVariantId(7L), 1)));

        verify(products, times(1)).save(family);
        assertThat(family.variant(new ProductVariantId(7L)).stockQuantity()).isEqualTo(4);
        assertThat(family.variant(new ProductVariantId(8L)).stockQuantity()).isEqualTo(4);
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
        verify(products, never()).findAllForPurchase(any());
        verify(products, never()).findById(any(ProductId.class));
        verify(products, never()).save(any());
    }

    private Product restoredProduct(long id, String sku, String name, String price, int stock) {
        return Product.restore(
                new ProductId(id),
                name,
                "Description",
                true,
                Set.of(),
                List.of(
                        ProductVariant.restore(
                                new ProductVariantId(id),
                                new Sku(sku),
                                new Money(new BigDecimal(price)),
                                stock,
                                "/images/product-placeholder.svg",
                                true,
                                true)));
    }
}
