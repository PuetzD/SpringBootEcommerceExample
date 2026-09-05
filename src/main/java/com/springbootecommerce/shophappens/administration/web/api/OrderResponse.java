package com.springbootecommerce.shophappens.administration.web.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        String id,
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
