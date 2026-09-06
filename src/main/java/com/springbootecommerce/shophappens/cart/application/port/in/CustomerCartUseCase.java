package com.springbootecommerce.shophappens.cart.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;

public interface CustomerCartUseCase {
    void changeQuantity(CustomerId customer, ProductVariantId variant, int quantity);

    void remove(CustomerId customer, ProductVariantId variant);

    CustomerCartSnapshot getSnapshot(CustomerId customer);
}
