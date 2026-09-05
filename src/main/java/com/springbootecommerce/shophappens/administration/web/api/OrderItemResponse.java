package com.springbootecommerce.shophappens.administration.web.api;

import java.math.BigDecimal;

public record OrderItemResponse(
        long productId,
        String sku,
        String productName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal) {}
