package com.springbootecommerce.shophappens.ordering.application.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.shophappens.ordering.domain.model.OrderItem;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrderPlacedIntegrationEventTest {
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
        assertThat(eventItem.currency()).isEqualTo("EUR");
        assertThat(eventItem.unitPrice()).isEqualByComparingTo("20.00");
        assertThat(eventItem.quantity()).isEqualTo(2);
        assertThat(OrderPlacedIntegrationEvent.EVENT_TYPE).isEqualTo("ordering.order-placed.v2");
    }
}
