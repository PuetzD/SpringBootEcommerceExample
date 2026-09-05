package com.springbootecommerce.shophappens.ordering.application.port.in;

public class OrderNotFoundException extends RuntimeException {
    private final String orderNumber;

    public OrderNotFoundException(String orderNumber) {
        super("Order not found: " + orderNumber);
        this.orderNumber = orderNumber;
    }

    public String orderNumber() {
        return orderNumber;
    }
}
