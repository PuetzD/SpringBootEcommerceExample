package com.springbootecommerce.shophappens.ordering.application.port.in;

public record OrderAddressView(
        String role,
        String recipientName,
        String companyName,
        String addressLine1,
        String addressLine2,
        String city,
        String region,
        String postalCode,
        String countryCode,
        String phoneNumber) {}
