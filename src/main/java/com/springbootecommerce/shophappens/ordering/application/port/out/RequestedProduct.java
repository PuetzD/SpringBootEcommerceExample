package com.springbootecommerce.shophappens.ordering.application.port.out;

import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;

public record RequestedProduct(ProductId productId, int quantity) {}
