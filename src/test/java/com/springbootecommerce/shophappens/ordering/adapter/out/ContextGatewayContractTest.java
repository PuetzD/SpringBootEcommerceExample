package com.springbootecommerce.shophappens.ordering.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.cart.application.port.in.CartItemSnapshot;
import com.springbootecommerce.shophappens.cart.application.port.in.ClearCustomerCartUseCase;
import com.springbootecommerce.shophappens.cart.application.port.in.CustomerCartQuery;
import com.springbootecommerce.shophappens.cart.application.port.in.CustomerCartSnapshot;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.PurchaseLine;
import com.springbootecommerce.shophappens.catalog.application.port.in.PurchaseProductsUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.PurchasedProductSnapshot;
import com.springbootecommerce.shophappens.ordering.adapter.out.cart.CustomerCartGatewayAdapter;
import com.springbootecommerce.shophappens.ordering.adapter.out.catalog.CatalogPurchaseGatewayAdapter;
import com.springbootecommerce.shophappens.ordering.application.port.out.CheckoutCart;
import com.springbootecommerce.shophappens.ordering.application.port.out.PurchasedProduct;
import com.springbootecommerce.shophappens.ordering.application.port.out.RequestedProduct;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContextGatewayContractTest {

    @Mock CustomerCartQuery cartQuery;
    @Mock ClearCustomerCartUseCase cartClear;
    @Mock PurchaseProductsUseCase purchase;

    @Test
    void mapsCartProductReferencesWithoutImportingCartDomain() {
        var cartGateway = new CustomerCartGatewayAdapter(cartQuery, cartClear);
        when(cartQuery.get(new CustomerId(42L)))
                .thenReturn(
                        new CustomerCartSnapshot(
                                new CustomerId(42L),
                                List.of(new CartItemSnapshot(new ProductId(7L), 2))));

        CheckoutCart result = cartGateway.load(new CustomerId(42L));

        assertThat(result.products()).containsExactly(new RequestedProduct(new ProductId(7L), 2));
    }

    @Test
    void mapsPurchasedCatalogFactsIntoOrderFacts() {
        var catalogGateway = new CatalogPurchaseGatewayAdapter(purchase);
        when(purchase.purchase(List.of(new PurchaseLine(new ProductReference(7L), 2))))
                .thenReturn(
                        List.of(
                                new PurchasedProductSnapshot(
                                        new ProductReference(7L),
                                        "ELEC-001",
                                        "Headphones",
                                        new Money(new BigDecimal("19.99")),
                                        2)));

        assertThat(catalogGateway.purchase(List.of(new RequestedProduct(new ProductId(7L), 2))))
                .containsExactly(purchasedProduct(7L, 2, "19.99"));
    }

    @Test
    void rejectsCatalogResultSetThatDiffersFromRequest() {
        var catalogGateway = new CatalogPurchaseGatewayAdapter(purchase);
        when(purchase.purchase(List.of(new PurchaseLine(new ProductReference(7L), 2))))
                .thenReturn(
                        List.of(
                                new PurchasedProductSnapshot(
                                        new ProductReference(7L),
                                        "ELEC-001",
                                        "Headphones",
                                        new Money(new BigDecimal("19.99")),
                                        1)));

        assertThatThrownBy(
                        () ->
                                catalogGateway.purchase(
                                        List.of(new RequestedProduct(new ProductId(7L), 2))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static PurchasedProduct purchasedProduct(long productId, int quantity, String price) {
        return new PurchasedProduct(
                new ProductId(productId),
                "ELEC-001",
                "Headphones",
                new Money(new BigDecimal(price)),
                quantity);
    }
}
