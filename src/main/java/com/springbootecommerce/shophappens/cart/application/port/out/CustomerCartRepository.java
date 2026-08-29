package com.springbootecommerce.shophappens.cart.application.port.out;

import com.springbootecommerce.shophappens.cart.domain.model.Cart;
import com.springbootecommerce.shophappens.cart.domain.model.CustomerId;
import java.util.Optional;

public interface CustomerCartRepository {
    Cart findOrCreate(CustomerId customerId);

    Optional<Cart> find(CustomerId customerId);

    Cart save(Cart cart);

    void clear(CustomerId customerId);
}
