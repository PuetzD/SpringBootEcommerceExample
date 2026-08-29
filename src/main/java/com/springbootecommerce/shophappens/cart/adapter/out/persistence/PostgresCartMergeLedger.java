package com.springbootecommerce.shophappens.cart.adapter.out.persistence;

import com.springbootecommerce.shophappens.cart.application.port.out.CartMergeLedger;
import com.springbootecommerce.shophappens.cart.domain.model.CustomerId;
import com.springbootecommerce.shophappens.cart.domain.model.GuestCartId;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class PostgresCartMergeLedger implements CartMergeLedger {
    private final JdbcTemplate jdbc;

    PostgresCartMergeLedger(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean claim(GuestCartId guestCartId, CustomerId customerId) {
        int inserted =
                jdbc.update(
                        "insert into consumed_guest_cart(guest_cart_id, customer_id) values (?, ?) "
                                + "on conflict (guest_cart_id) do nothing",
                        guestCartId.value(),
                        customerId.value());
        return inserted == 1;
    }
}
