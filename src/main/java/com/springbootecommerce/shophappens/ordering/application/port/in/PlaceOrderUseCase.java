package com.springbootecommerce.shophappens.ordering.application.port.in;

public interface PlaceOrderUseCase {
    PlacedOrder place(PlaceOrderCommand command);
}
