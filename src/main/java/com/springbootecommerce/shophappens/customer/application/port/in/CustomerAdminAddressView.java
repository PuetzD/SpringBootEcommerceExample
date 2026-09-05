package com.springbootecommerce.shophappens.customer.application.port.in;

import com.springbootecommerce.shophappens.customer.domain.model.AddressId;

public record CustomerAdminAddressView(
        AddressId addressId,
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
