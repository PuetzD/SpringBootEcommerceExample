package com.springbootecommerce.shophappens.ordering.application.port.out;

import com.springbootecommerce.shophappens.ordering.domain.model.ProductId;

public record RequestedProduct(ProductId productId, int quantity) {}
