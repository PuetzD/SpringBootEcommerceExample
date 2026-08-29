package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import com.springbootecommerce.shophappens.ordering.application.port.out.CheckoutLock;
import com.springbootecommerce.shophappens.ordering.domain.model.CheckoutId;
import com.springbootecommerce.shophappens.ordering.domain.model.CustomerId;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class JdbcCheckoutLockAdapter implements CheckoutLock {
    private final JdbcTemplate jdbc;

    JdbcCheckoutLockAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void acquire(CustomerId customerId, CheckoutId checkoutId) {
        int inserted =
                jdbc.update(
                        "insert into checkout_lock(customer_id, checkout_id) values (?, ?)",
                        customerId.value(),
                        checkoutId.value());
        if (inserted == 0) {
            throw new IllegalStateException(
                    "Checkout already in progress for customer "
                            + customerId.value()
                            + " checkout "
                            + checkoutId.value());
        }
    }
}
