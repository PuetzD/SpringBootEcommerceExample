package com.springbootecommerce.shophappens.ordering.domain.model;

import com.springbootecommerce.shophappens.ordering.domain.exception.EmptyCheckoutException;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

public record Order(
        OrderId orderId,
        OrderNumber orderNumber,
        CheckoutId checkoutId,
        CustomerId customerId,
        List<OrderItem> items,
        OrderAddress shippingAddress,
        OrderAddress billingAddress,
        Instant placedAt,
        Money total) {
    public Order {
        items = Collections.unmodifiableList(items);
        if (items.isEmpty()) {
            throw new EmptyCheckoutException();
        }
        if (shippingAddress == null || shippingAddress.role() != AddressRole.SHIPPING) {
            throw new IllegalArgumentException("Shipping address is required");
        }
        if (billingAddress == null || billingAddress.role() != AddressRole.BILLING) {
            throw new IllegalArgumentException("Billing address is required");
        }
    }

    public static Order place(
            OrderId orderId,
            OrderNumber orderNumber,
            CheckoutId checkoutId,
            CustomerId customerId,
            List<OrderItem> items,
            OrderAddress shippingAddress,
            OrderAddress billingAddress,
            Instant placedAt) {
        Money total = items.stream().map(OrderItem::lineTotal).reduce(Money.zero(), Money::add);
        return new Order(
                orderId,
                orderNumber,
                checkoutId,
                customerId,
                items,
                shippingAddress,
                billingAddress,
                placedAt,
                total);
    }

    public static Order restore(
            OrderId orderId,
            OrderNumber orderNumber,
            CheckoutId checkoutId,
            CustomerId customerId,
            List<OrderItem> items,
            OrderAddress shippingAddress,
            OrderAddress billingAddress,
            Instant placedAt,
            Money total) {
        return new Order(
                orderId,
                orderNumber,
                checkoutId,
                customerId,
                items,
                shippingAddress,
                billingAddress,
                placedAt,
                total);
    }
}
