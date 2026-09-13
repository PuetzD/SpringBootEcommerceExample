package com.springbootecommerce.shophappens.ordering.application.event;

import com.springbootecommerce.shophappens.ordering.domain.model.Order;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderAddress;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderItem;
import com.springbootecommerce.shophappens.sharedkernel.money.Currency;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderPlacedIntegrationEvent(
        UUID eventId,
        UUID orderId,
        String orderNumber,
        long customerId,
        String customerGivenName,
        String customerContactEmail,
        Instant occurredAt,
        BigDecimal total,
        Currency currency,
        List<Item> items,
        Address shippingAddress,
        Address billingAddress) {
    public static final String EVENT_TYPE = "ordering.order-placed.v1";

    public OrderPlacedIntegrationEvent {
        items = List.copyOf(items);
    }

    public static OrderPlacedIntegrationEvent from(
            Order order, String customerGivenName, String customerContactEmail) {
        return new OrderPlacedIntegrationEvent(
                UUID.randomUUID(),
                order.orderId().value(),
                order.orderNumber().value(),
                order.customerId().value(),
                customerGivenName,
                customerContactEmail,
                order.placedAt(),
                order.total().amount(),
                order.total().currency(),
                order.items().stream().map(Item::from).toList(),
                Address.from(order.shippingAddress()),
                Address.from(order.billingAddress()));
    }

    public record Item(
            long variantId,
            long productId,
            String sku,
            String productName,
            BigDecimal unitPrice,
            int quantity) {
        static Item from(OrderItem item) {
            return new Item(
                    item.variantId().value(),
                    item.productId().value(),
                    item.sku(),
                    item.productName(),
                    item.unitPrice().amount(),
                    item.quantity());
        }
    }

    public record Address(
            String recipientName,
            String companyName,
            String addressLine1,
            String addressLine2,
            String city,
            String region,
            String postalCode,
            String countryCode,
            String phoneNumber) {
        private static Address from(OrderAddress address) {
            return new Address(
                    address.recipientName(),
                    address.companyName(),
                    address.addressLine1(),
                    address.addressLine2(),
                    address.city(),
                    address.region(),
                    address.postalCode(),
                    address.countryCode(),
                    address.phoneNumber());
        }
    }
}
