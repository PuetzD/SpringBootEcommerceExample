package com.springbootecommerce.shophappens.ordering.adapter.out.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.catalog.application.port.in.BrowseCatalogUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductSummary;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutItem;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckoutCatalogGatewayAdapterTest {
    private static final ProductVariantId BLUE = new ProductVariantId(202);

    @Mock BrowseCatalogUseCase catalog;
    @InjectMocks CheckoutCatalogGatewayAdapter gateway;

    @Test
    void mapsTheSelectedActiveVariantToOrderingFacts() {
        when(catalog.findActiveByVariantId(BLUE))
                .thenReturn(
                        Optional.of(
                                new ProductSummary(
                                        new ProductReference(7),
                                        "BLUE",
                                        "Blue shirt",
                                        "A blue shirt",
                                        new Money(new BigDecimal("20.00")),
                                        5,
                                        "/blue.jpg",
                                        BLUE)));

        assertThat(gateway.find(BLUE, 3))
                .contains(
                        new CheckoutItem(
                                BLUE,
                                new ProductId(7),
                                "BLUE",
                                "Blue shirt",
                                new Money(new BigDecimal("20.00")),
                                3));
    }

    @Test
    void returnsEmptyWhenTheSelectedVariantIsUnavailable() {
        ProductVariantId unavailable = new ProductVariantId(203);
        when(catalog.findActiveByVariantId(unavailable)).thenReturn(Optional.empty());

        assertThat(gateway.find(unavailable, 1)).isEmpty();
    }

    @Test
    void calculatesLineTotalFromReviewedVariantPrice() {
        var item =
                new CheckoutItem(
                        BLUE,
                        new ProductId(7),
                        "BLUE",
                        "Blue shirt",
                        new Money(new BigDecimal("20.00")),
                        3);

        assertThat(item.lineTotal()).isEqualTo(new Money(new BigDecimal("60.00")));
    }
}
