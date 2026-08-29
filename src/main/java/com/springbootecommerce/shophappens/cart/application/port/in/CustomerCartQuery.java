package com.springbootecommerce.shophappens.cart.application.port.in;

import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;

public interface CustomerCartQuery {
    CustomerCartSnapshot get(CustomerReference customer);
}
