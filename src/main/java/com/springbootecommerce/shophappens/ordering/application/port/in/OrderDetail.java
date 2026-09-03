package com.springbootecommerce.shophappens.ordering.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.time.Instant;
import java.util.List;

public record OrderDetail(
        OrderReference order,
        String orderNumber,
        Money total,
        Instant placedAt,
        List<OrderItemDetail> items,
        List<OrderAddressDetail> addresses) {
    public OrderDetail {
        items = List.copyOf(items);
        addresses = List.copyOf(addresses);
    }
}
