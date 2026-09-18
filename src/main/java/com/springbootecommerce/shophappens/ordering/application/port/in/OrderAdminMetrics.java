package com.springbootecommerce.shophappens.ordering.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.util.Objects;

public record OrderAdminMetrics(Money revenue) {
    public OrderAdminMetrics {
        Objects.requireNonNull(revenue, "revenue must not be null");
    }
}
