package com.springbootecommerce.shophappens.ordering.application.port.in;

import com.springbootecommerce.shophappens.ordering.domain.model.OrderAddress;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderItem;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

public record OrderDetail(
        OrderReference order,
        String orderNumber,
        Money total,
        Instant placedAt,
        List<OrderItem> items,
        List<OrderAddress> addresses) {
    public OrderDetail {
        items = List.copyOf(items);
        addresses = List.copyOf(addresses);
    }
}
