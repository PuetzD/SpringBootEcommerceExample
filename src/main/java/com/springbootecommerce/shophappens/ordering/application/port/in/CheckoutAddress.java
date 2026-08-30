package com.springbootecommerce.shophappens.ordering.application.port.in;

import com.springbootecommerce.shophappens.customer.application.port.in.AddressReference;

public record CheckoutAddress(
        AddressReference address,
        String recipientName,
        String city,
        String postalCode,
        String countryCode,
        boolean defaultShipping,
        boolean defaultBilling) {}
