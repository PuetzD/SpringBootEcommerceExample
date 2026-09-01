package com.springbootecommerce.shophappens.ordering.application.event;

import com.springbootecommerce.shophappens.ordering.domain.model.Order;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderAddress;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderPlacedIntegrationEvent(
        UUID eventId,
        UUID orderId,
        String orderNumber,
        long customerId,
        Instant occurredAt,
        BigDecimal total,
        List<Item> items,
        Address shippingAddress,
        Address billingAddress) {
    public static final String EVENT_TYPE = "ordering.order-placed.v1";

    public OrderPlacedIntegrationEvent {
        items = List.copyOf(items);
    }

    public static OrderPlacedIntegrationEvent from(Order order) {
        return new OrderPlacedIntegrationEvent(
                UUID.randomUUID(),
                order.orderId().value(),
                order.orderNumber().value(),
                order.customerId().value(),
                order.placedAt(),
                order.total().amount(),
                order.items().stream().map(Item::from).toList(),
                Address.from(order.shippingAddress()),
                Address.from(order.billingAddress()));
    }

    public record Item(
            long productId, String sku, String productName, BigDecimal unitPrice, int quantity) {
        private static Item from(OrderItem item) {
            return new Item(
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
