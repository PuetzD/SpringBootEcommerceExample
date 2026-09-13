package com.springbootecommerce.shophappens.administration.web.api;

import java.math.BigDecimal;

public record OrderItemResponse(
        long variantId,
        long productId,
        String sku,
        String productName,
        BigDecimal unitPrice,
        String currency,
        int quantity,
        BigDecimal lineTotal) {}
