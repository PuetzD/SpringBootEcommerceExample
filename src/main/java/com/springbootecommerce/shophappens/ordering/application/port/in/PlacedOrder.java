package com.springbootecommerce.shophappens.ordering.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.time.Instant;

public record PlacedOrder(
        OrderReference order, String orderNumber, Money total, Instant placedAt) {}
