package com.springbootecommerce.shophappens.ordering.application.port.in;

public record CheckoutAddress(
        long addressId,
        String recipientName,
        String city,
        String postalCode,
        String countryCode,
        boolean defaultShipping,
        boolean defaultBilling) {}
