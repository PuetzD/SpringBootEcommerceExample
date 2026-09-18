package com.springbootecommerce.shophappens.ordering.notification.application.port.in;

import com.springbootecommerce.shophappens.ordering.application.event.OrderPlacedIntegrationEvent;

@FunctionalInterface
public interface SendOrderConfirmationUseCase {
    void send(OrderPlacedIntegrationEvent event);
}
