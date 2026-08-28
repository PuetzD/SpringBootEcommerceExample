package com.springbootecommerce.shophappens.customer.application;

public record AddressSnapshot(
        Long id,
        String recipientName,
        String companyName,
        String addressLine1,
        String addressLine2,
        String city,
        String region,
        String postalCode,
        String countryCode,
        String phoneNumber,
        boolean defaultShipping,
        boolean defaultBilling) {}
