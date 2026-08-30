package com.springbootecommerce.shophappens.ordering.application.port.in;

import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import java.util.List;
import java.util.Optional;

public interface OrderQuery {
    Optional<OrderDetail> findOwned(CustomerReference customer, String orderNumber);

    List<OrderSummary> findAll(CustomerReference customer);
}
