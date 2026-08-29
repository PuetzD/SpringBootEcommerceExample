package com.springbootecommerce.shophappens.ordering.application.port.in;

public record CheckoutAddress(
        AddressReference address,
        String recipientName,
        String city,
        String postalCode,
        String countryCode,
        boolean defaultShipping,
        boolean defaultBilling) {}
