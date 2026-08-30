package com.springbootecommerce.shophappens.ordering.application.port.in;

import com.springbootecommerce.shophappens.customer.application.port.in.AddressReference;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;

public record PlaceOrderCommand(
        CustomerReference customer,
        CheckoutReference checkout,
        AddressReference shippingAddress,
        AddressReference billingAddress) {}
