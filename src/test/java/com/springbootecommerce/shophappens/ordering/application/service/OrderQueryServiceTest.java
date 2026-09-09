package com.springbootecommerce.shophappens.ordering.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutAddress;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutItem;
import com.springbootecommerce.shophappens.ordering.application.port.out.AvailableAddress;
import com.springbootecommerce.shophappens.ordering.application.port.out.CheckoutCart;
import com.springbootecommerce.shophappens.ordering.application.port.out.CheckoutCatalogGateway;
import com.springbootecommerce.shophappens.ordering.application.port.out.CustomerAddressGateway;
import com.springbootecommerce.shophappens.ordering.application.port.out.CustomerCartGateway;
import com.springbootecommerce.shophappens.ordering.application.port.out.OrderRepository;
import com.springbootecommerce.shophappens.ordering.application.port.out.RequestedProduct;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderQueryServiceTest {
    private static final CustomerId CUSTOMER = new CustomerId(42);
    private static final ProductVariantId BLUE = new ProductVariantId(202);
    private static final ProductVariantId RED = new ProductVariantId(205);
    private static final ProductVariantId MISSING = new ProductVariantId(203);
    private static final ProductVariantId INACTIVE = new ProductVariantId(204);

    @Mock CustomerCartGateway carts;
    @Mock CustomerAddressGateway addresses;
    @Mock OrderRepository orders;
    @Mock CheckoutCatalogGateway catalog;
    @InjectMocks OrderQueryService service;

    @Test
    void preparesAvailableFactsTotalUnavailableVariantsAndAddresses() {
        var blue = item(BLUE, 7, "BLUE", "Blue shirt", "20.00", 3);
        var red = item(RED, 8, "RED", "Red socks", "7.50", 2);
        when(carts.load(CUSTOMER))
                .thenReturn(
                        new CheckoutCart(
                                List.of(
                                        new RequestedProduct(BLUE, 3),
                                        new RequestedProduct(MISSING, 1),
                                        new RequestedProduct(RED, 2),
                                        new RequestedProduct(INACTIVE, 1))));
        when(catalog.find(BLUE, 3)).thenReturn(Optional.of(blue));
        when(catalog.find(MISSING, 1)).thenReturn(Optional.empty());
        when(catalog.find(RED, 2)).thenReturn(Optional.of(red));
        when(catalog.find(INACTIVE, 1)).thenReturn(Optional.empty());
        when(addresses.available(CUSTOMER))
                .thenReturn(
                        List.of(
                                new AvailableAddress(
                                        11,
                                        "Daniela Puetz",
                                        "Berlin",
                                        "10115",
                                        "DE",
                                        true,
                                        false)));

        var result = service.prepare(CUSTOMER);

        assertThat(result.customer()).isEqualTo(CUSTOMER);
        assertThat(result.items()).containsExactly(blue, red);
        assertThat(result.unavailableVariants()).containsExactly(MISSING, INACTIVE);
        assertThat(result.merchandiseTotal()).isEqualTo(new Money(new BigDecimal("75.00")));
        assertThat(result.addresses())
                .containsExactly(
                        new CheckoutAddress(
                                11, "Daniela Puetz", "Berlin", "10115", "DE", true, false));
    }

    private static CheckoutItem item(
            ProductVariantId variant,
            long product,
            String sku,
            String name,
            String price,
            int quantity) {
        return new CheckoutItem(
                variant,
                new ProductId(product),
                sku,
                name,
                new Money(new BigDecimal(price)),
                quantity);
    }
}
