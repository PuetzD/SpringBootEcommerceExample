package com.springbootecommerce.shophappens.ordering.domain.model;

import java.util.Objects;

public record OrderNumber(String value) {
    public OrderNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Order number must not be blank");
        }
        if (value.length() > 32) {
            throw new IllegalArgumentException("Order number must not exceed 32 characters");
        }
        if (!value.matches("ORD-\\d{8}-[A-Z0-9]{12}")) {
            throw new IllegalArgumentException("Order number format is invalid");
        }
        value = value.strip();
    }
}
