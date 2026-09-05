package com.springbootecommerce.shophappens.ordering.application.port.in;

import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.List;
import java.util.Optional;

public interface OrderAdministrationQuery {
    OrderAdminPage searchOrders(OrderAdminSearch search);

    Optional<OrderAdminDetail> findOrder(String orderNumber);

    List<OrderAdminSummary> findOrdersForCustomer(CustomerId customerId);
}
