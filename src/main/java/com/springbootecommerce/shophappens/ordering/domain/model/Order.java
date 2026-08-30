package com.springbootecommerce.shophappens.ordering.domain.model;

import com.springbootecommerce.shophappens.ordering.domain.exception.EmptyCheckoutException;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

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
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(orderNumber, "orderNumber must not be null");
        Objects.requireNonNull(checkoutId, "checkoutId must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(items, "items must not be null");
        Objects.requireNonNull(shippingAddress, "shippingAddress must not be null");
        Objects.requireNonNull(billingAddress, "billingAddress must not be null");
        Objects.requireNonNull(placedAt, "placedAt must not be null");
        Objects.requireNonNull(total, "total must not be null");
        items = List.copyOf(items);
        if (items.isEmpty()) {
            throw new EmptyCheckoutException();
        }
        if (shippingAddress.role() != AddressRole.SHIPPING) {
            throw new IllegalArgumentException("Shipping address must have role SHIPPING");
        }
        if (billingAddress.role() != AddressRole.BILLING) {
            throw new IllegalArgumentException("Billing address must have role BILLING");
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
