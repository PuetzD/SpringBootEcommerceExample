package com.springbootecommerce.shophappens.cart.application.port.in;

import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;

public interface CustomerCartUseCase {
    void changeQuantity(CustomerReference customer, ProductReference product, int quantity);

    void remove(CustomerReference customer, ProductReference product);

    CustomerCartSnapshot getSnapshot(CustomerReference customer);
}
