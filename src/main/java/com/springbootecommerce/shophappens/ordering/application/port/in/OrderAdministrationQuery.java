package com.springbootecommerce.shophappens.ordering.application.port.in;

import java.util.Optional;

public interface OrderAdministrationQuery {
    OrderAdminPage searchOrders(OrderAdminSearch search);

    Optional<OrderAdminDetail> findOrder(String orderNumber);
}
