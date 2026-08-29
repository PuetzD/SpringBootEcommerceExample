package com.springbootecommerce.shophappens.cart.adapter.out.redis;

import java.util.List;
import java.util.UUID;

record GuestCartDocument(UUID cartId, UUID guestCartId, List<Item> items) {

    record Item(long productId, int quantity) {}
}
