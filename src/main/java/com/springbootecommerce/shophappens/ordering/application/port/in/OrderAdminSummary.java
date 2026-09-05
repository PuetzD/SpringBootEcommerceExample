package com.springbootecommerce.shophappens.ordering.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.time.Instant;

public record OrderAdminSummary(
        OrderReference order,
        String orderNumber,
        CustomerId customerId,
        Money total,
        Instant placedAt) {}
