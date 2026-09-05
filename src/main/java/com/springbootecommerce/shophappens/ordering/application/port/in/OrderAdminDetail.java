package com.springbootecommerce.shophappens.ordering.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.time.Instant;
import java.util.List;

public record OrderAdminDetail(
        OrderReference order,
        String orderNumber,
        CustomerId customerId,
        Money total,
        Instant placedAt,
        List<OrderItemView> items,
        List<OrderAddressView> addresses) {
    public OrderAdminDetail {
        items = List.copyOf(items);
        addresses = List.copyOf(addresses);
    }
}
