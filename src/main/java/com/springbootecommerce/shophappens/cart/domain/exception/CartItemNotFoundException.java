package com.springbootecommerce.shophappens.cart.domain.exception;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class CartItemNotFoundException extends RuntimeException {
    private final ProductId productId;
}
