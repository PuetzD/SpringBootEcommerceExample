package com.springbootecommerce.shophappens.ordering.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.springbootecommerce.shophappens.ordering.domain.exception.EmptyCheckoutException;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderTest {

    @Test
    void placesOrderFromImmutableCommercialAndPostalFacts() {
        Order order =
                Order.place(
                        OrderId.random(),
                        new OrderNumber("ORD-20260828-ABC123DEF456"),
                        new CheckoutId(UUID.randomUUID()),
                        new CustomerId(42L),
                        List.of(
                                new OrderItem(
                                        new ProductId(7L),
                                        "ELEC-001",
                                        "Headphones",
                                        new Money(new BigDecimal("19.99")),
                                        2)),
                        shippingAddress(),
                        billingAddress(),
                        Instant.parse("2026-08-28T08:00:00Z"));

        assertThat(order.total()).isEqualTo(new Money(new BigDecimal("39.98")));
        assertThat(order.items()).hasSize(1).isUnmodifiable();
        assertThat(order.shippingAddress().role()).isEqualTo(AddressRole.SHIPPING);
    }

    @Test
    void rejectsEmptyCheckoutAndMismatchedAddressRoles() {
        assertThatThrownBy(() -> placeWithItems(List.of()))
                .isInstanceOf(EmptyCheckoutException.class);
        assertThatThrownBy(() -> placeWithAddresses(billingAddress(), billingAddress()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Order placeWithItems(List<OrderItem> items) {
        return Order.place(
                OrderId.random(),
                new OrderNumber("ORD-20260828-ABC123DEF456"),
                new CheckoutId(UUID.randomUUID()),
                new CustomerId(42L),
                items,
                shippingAddress(),
                billingAddress(),
                Instant.parse("2026-08-28T08:00:00Z"));
    }

    private Order placeWithAddresses(OrderAddress shipping, OrderAddress billing) {
        return Order.place(
                OrderId.random(),
                new OrderNumber("ORD-20260828-ABC123DEF456"),
                new CheckoutId(UUID.randomUUID()),
                new CustomerId(42L),
                List.of(
                        new OrderItem(
                                new ProductId(7L),
                                "ELEC-001",
                                "Headphones",
                                new Money(new BigDecimal("19.99")),
                                2)),
                shipping,
                billing,
                Instant.parse("2026-08-28T08:00:00Z"));
    }

    private OrderAddress shippingAddress() {
        return new OrderAddress(
                AddressRole.SHIPPING,
                "Jane Doe",
                "Acme Inc",
                "123 Main St",
                "Apt 4B",
                "Metropolis",
                "NY",
                "10001",
                "US",
                "+1-555-0100");
    }

    private OrderAddress billingAddress() {
        return new OrderAddress(
                AddressRole.BILLING,
                "Jane Doe",
                "Acme Inc",
                "456 Oak Ave",
                null,
                "Metropolis",
                "NY",
                "10001",
                "US",
                "+1-555-0100");
    }
}
