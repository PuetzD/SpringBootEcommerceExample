package com.springbootecommerce.shophappens.ordering.application.port.out;

import com.springbootecommerce.shophappens.customer.application.port.in.AddressReference;

public record AvailableAddress(
        AddressReference address,
        String recipientName,
        String city,
        String postalCode,
        String countryCode,
        boolean defaultShipping,
        boolean defaultBilling) {}
