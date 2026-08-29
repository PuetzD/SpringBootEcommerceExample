package com.springbootecommerce.shophappens.ordering.application.port.in;

public record PlaceOrderCommand(
        CustomerReference customer,
        CheckoutReference checkout,
        AddressReference shippingAddress,
        AddressReference billingAddress) {}
