package com.springbootecommerce.shophappens.administration.web.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        String id,
        UUID orderId,
        String orderNumber,
        long customerId,
        BigDecimal total,
        Instant placedAt,
        List<OrderItemResponse> items,
        List<OrderAddressResponse> addresses) {
    public OrderResponse {
        items = List.copyOf(items == null ? List.of() : items);
        addresses = List.copyOf(addresses == null ? List.of() : addresses);
    }
}
