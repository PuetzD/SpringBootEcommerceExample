package com.springbootecommerce.shophappens.cart.application.port.out;

import com.springbootecommerce.shophappens.cart.domain.model.Cart;
import com.springbootecommerce.shophappens.cart.domain.model.GuestCartId;
import java.util.Optional;

public interface GuestCartRepository {
    long NEW_CART = -1L;

    Optional<Cart> find(GuestCartId id);

    Cart save(Cart cart, long expectedVersion);

    void delete(GuestCartId id);
}
