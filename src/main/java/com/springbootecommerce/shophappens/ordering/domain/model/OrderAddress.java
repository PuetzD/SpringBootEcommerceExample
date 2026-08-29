package com.springbootecommerce.shophappens.ordering.domain.model;

import java.util.Objects;

public record OrderAddress(
        AddressRole role,
        String recipientName,
        String companyName,
        String addressLine1,
        String addressLine2,
        String city,
        String region,
        String postalCode,
        String countryCode,
        String phoneNumber) {
    public OrderAddress {
        role = Objects.requireNonNull(role);
        recipientName = required(recipientName);
        addressLine1 = required(addressLine1);
        city = required(city);
        postalCode = required(postalCode);
        if (countryCode == null || !countryCode.matches("[A-Z]{2}")) {
            throw new IllegalArgumentException("Country code must be two uppercase letters");
        }
    }

    private static String required(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Address field must not be blank");
        }
        return value.strip();
    }
}
