package com.springbootecommerce.shophappens.ordering.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;

public record PlaceOrderCommand(
        CustomerId customer,
        CheckoutReference checkout,
        long shippingAddress,
        long billingAddress) {}
