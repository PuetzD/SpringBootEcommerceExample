package com.springbootecommerce.shophappens.cart.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;

public interface CustomerCartUseCase {
    void changeQuantity(CustomerId customer, ProductId product, int quantity);

    void remove(CustomerId customer, ProductId product);

    CustomerCartSnapshot getSnapshot(CustomerId customer);
}
