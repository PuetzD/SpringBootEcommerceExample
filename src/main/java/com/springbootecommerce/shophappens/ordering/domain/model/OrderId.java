package com.springbootecommerce.shophappens.ordering.domain.model;

import java.util.UUID;

public record OrderId(UUID value) {
    public OrderId {
        if (value == null) throw new IllegalArgumentException("Order ID must not be null");
    }

    public static OrderId random() {
        return new OrderId(UUID.randomUUID());
    }
}
