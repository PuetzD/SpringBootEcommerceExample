package com.springbootecommerce.shophappens.ordering.notification.application.port.out;

import com.springbootecommerce.shophappens.ordering.notification.application.port.in.OrderConfirmationDeliveryView;
import java.util.Optional;

public interface OrderConfirmationDeliveryQuery {
    Optional<OrderConfirmationDeliveryView> findByOrderNumber(String orderNumber);
}
