package com.springbootecommerce.shophappens.ordering.application.port.out;

public record AvailableAddress(
        AddressReference address,
        String recipientName,
        String city,
        String postalCode,
        String countryCode,
        boolean defaultShipping,
        boolean defaultBilling) {}
