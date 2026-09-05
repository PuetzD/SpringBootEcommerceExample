package com.springbootecommerce.shophappens.administration.web.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CustomerOrderResponse(
        String orderNumber, UUID orderId, BigDecimal total, Instant placedAt, String orderUrl) {}
