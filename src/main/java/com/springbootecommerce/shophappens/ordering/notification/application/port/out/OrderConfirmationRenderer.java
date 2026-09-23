package com.springbootecommerce.shophappens.ordering.notification.application.port.out;

import com.springbootecommerce.shophappens.ordering.application.event.OrderPlacedIntegrationEvent;

@FunctionalInterface
public interface OrderConfirmationRenderer {
    RenderedOrderConfirmation render(OrderPlacedIntegrationEvent event);
}
