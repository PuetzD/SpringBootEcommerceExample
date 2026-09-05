package com.springbootecommerce.shophappens.ordering.application.service;

import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminDetail;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminPage;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminSearch;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdministrationQuery;
import java.util.Optional;

public class OrderAdministrationQueryService implements OrderAdministrationQuery {
    @Override
    public OrderAdminPage searchOrders(OrderAdminSearch search) {
        return null;
    }

    @Override
    public Optional<OrderAdminDetail> findOrder(String orderNumber) {
        return Optional.empty();
    }
}
