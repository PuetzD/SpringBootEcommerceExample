package com.springbootecommerce.shophappens.ordering.application.port.in;

import com.springbootecommerce.shophappens.cart.application.port.in.CartItemSnapshot;
import com.springbootecommerce.shophappens.customer.application.port.in.AddressSnapshot;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.time.Instant;

public record OrderAdminDetail(
        String OrderNumber,
        CustomerId customerId,
        Money total,
        Instant placedAt,
        AddressSnapshot shippingAddress,
        AddressSnapshot billingAddress,
        CartItemSnapshot[] items) {}
