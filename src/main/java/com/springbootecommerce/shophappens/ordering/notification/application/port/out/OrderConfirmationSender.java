package com.springbootecommerce.shophappens.ordering.notification.application.port.out;

@FunctionalInterface
public interface OrderConfirmationSender {
    void send(RenderedOrderConfirmation confirmation);
}
