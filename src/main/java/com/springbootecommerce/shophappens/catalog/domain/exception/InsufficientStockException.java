package com.springbootecommerce.shophappens.catalog.domain.exception;

import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class InsufficientStockException extends RuntimeException {
    private final ProductId productId;
    private final Sku sku;
    private final int requestedQuantity;
    private final int availableQuantity;
}
