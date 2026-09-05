package com.springbootecommerce.shophappens.ordering.application.service;

import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminDetail;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminPage;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminSearch;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminSummary;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdministrationQuery;
import com.springbootecommerce.shophappens.ordering.application.port.out.OrderRepository;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderAdministrationQueryService implements OrderAdministrationQuery {

    private final OrderRepository orderRepository;

    @Override
    public OrderAdminPage searchOrders(OrderAdminSearch search) {
        return orderRepository.searchForAdministration(search);
    }

    @Override
    public Optional<OrderAdminDetail> findOrder(String orderNumber) {
        return orderRepository.findForAdministration(orderNumber);
    }

    @Override
    public List<OrderAdminSummary> findOrdersForCustomer(CustomerId customerId) {
        return orderRepository.findOrdersForCustomer(customerId);
    }
}
