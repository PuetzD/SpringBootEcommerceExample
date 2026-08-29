package com.springbootecommerce.shophappens.cart.domain.model;

public sealed interface CartOwner permits CartOwner.Customer, CartOwner.Guest {
    record Customer(CustomerId id) implements CartOwner {}

    record Guest(GuestCartId id) implements CartOwner {}
}
