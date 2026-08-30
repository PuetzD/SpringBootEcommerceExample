package com.springbootecommerce.shophappens.cart.domain.model;

import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;

public sealed interface CartOwner permits CartOwner.Customer, CartOwner.Guest {
    record Customer(CustomerId id) implements CartOwner {}

    record Guest(GuestCartId id) implements CartOwner {}
}
