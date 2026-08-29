package com.springbootecommerce.shophappens.ordering.application.port.out;

import com.springbootecommerce.shophappens.ordering.domain.model.OrderNumber;

public interface OrderNumberGenerator {
    OrderNumber next();
}
