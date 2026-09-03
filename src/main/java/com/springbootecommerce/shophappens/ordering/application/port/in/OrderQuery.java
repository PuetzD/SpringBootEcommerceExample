package com.springbootecommerce.shophappens.ordering.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.List;
import java.util.Optional;

public interface OrderQuery {
    Optional<OrderDetail> findOwned(CustomerId customer, String orderNumber);

    List<OrderSummary> findAll(CustomerId customer);
}
