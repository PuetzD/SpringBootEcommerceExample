package com.springbootecommerce.shophappens.ordering.application.port.out;

import com.springbootecommerce.shophappens.ordering.domain.model.CheckoutId;
import com.springbootecommerce.shophappens.ordering.domain.model.Order;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderId;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Optional<Order> findByCheckout(CustomerId customerId, CheckoutId checkoutId);

    Optional<Order> findById(OrderId orderId);

    Optional<Order> findOwnedByOrderNumber(CustomerId customerId, String orderNumber);

    List<Order> findAllByCustomer(CustomerId customerId);

    Order save(Order order);
}
