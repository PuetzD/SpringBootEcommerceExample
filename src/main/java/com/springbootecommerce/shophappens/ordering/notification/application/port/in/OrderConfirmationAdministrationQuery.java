package com.springbootecommerce.shophappens.ordering.notification.application.port.in;

import java.util.Optional;

public interface OrderConfirmationAdministrationQuery {
    Optional<OrderConfirmationDeliveryView> findForOrderNumber(String orderNumber);
}
