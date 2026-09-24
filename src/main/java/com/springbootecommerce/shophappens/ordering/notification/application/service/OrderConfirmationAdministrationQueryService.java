package com.springbootecommerce.shophappens.ordering.notification.application.service;

import com.springbootecommerce.shophappens.ordering.notification.application.port.in.OrderConfirmationAdministrationQuery;
import com.springbootecommerce.shophappens.ordering.notification.application.port.in.OrderConfirmationDeliveryView;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationDeliveryQuery;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderConfirmationAdministrationQueryService
        implements OrderConfirmationAdministrationQuery {
    private final OrderConfirmationDeliveryQuery query;

    @Override
    public Optional<OrderConfirmationDeliveryView> findForOrderNumber(String orderNumber) {
        return query.findByOrderNumber(orderNumber);
    }
}
