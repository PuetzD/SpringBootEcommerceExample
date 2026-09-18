package com.springbootecommerce.shophappens.ordering.application.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.shophappens.ordering.domain.model.OrderItem;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderPlacedIntegrationEventTest {
    @Test
    void carriesTheOrderConfirmationRecipientSnapshot() {
        var event =
                new OrderPlacedIntegrationEvent(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "ORD-2026-100001",
                        42L,
                        "Ada",
                        "ada@example.com",
                        Instant.parse("2026-08-31T10:15:30Z"),
                        new BigDecimal("39.98"),
                        com.springbootecommerce.shophappens.sharedkernel.money.Currency.EUR,
                        List.of(),
                        new OrderPlacedIntegrationEvent.Address(
                                "Ada",
                                null,
                                "1 Main Street",
                                null,
                                "Berlin",
                                null,
                                "10115",
                                "DE",
                                null),
                        new OrderPlacedIntegrationEvent.Address(
                                "Ada",
                                null,
                                "1 Main Street",
                                null,
                                "Berlin",
                                null,
                                "10115",
                                "DE",
                                null));

        assertThat(event.customerGivenName()).isEqualTo("Ada");
        assertThat(event.customerContactEmail()).isEqualTo("ada@example.com");
    }

    @Test
    void snapshotsTheSelectedSellableAndCurrency() {
        var item =
                new OrderItem(
                        new ProductVariantId(202),
                        new ProductId(7),
                        "SHIRT-L",
                        "Shirt",
                        new Money(new BigDecimal("20.00")),
                        2);

        var eventItem = OrderPlacedIntegrationEvent.Item.from(item);

        assertThat(eventItem.variantId()).isEqualTo(202);
        assertThat(eventItem.productId()).isEqualTo(7);
        assertThat(eventItem.unitPrice()).isEqualByComparingTo("20.00");
        assertThat(eventItem.quantity()).isEqualTo(2);
        assertThat(OrderPlacedIntegrationEvent.EVENT_TYPE).isEqualTo("ordering.order-placed.v1");
    }
}
