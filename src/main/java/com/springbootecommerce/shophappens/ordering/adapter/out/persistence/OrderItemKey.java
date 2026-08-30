package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
class OrderItemKey implements Serializable {
    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "line_number")
    private int lineNumber;

    protected OrderItemKey() {}

    OrderItemKey(UUID orderId, int lineNumber) {
        this.orderId = orderId;
        this.lineNumber = lineNumber;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof OrderItemKey key
                && Objects.equals(orderId, key.orderId)
                && lineNumber == key.lineNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, lineNumber);
    }
}
