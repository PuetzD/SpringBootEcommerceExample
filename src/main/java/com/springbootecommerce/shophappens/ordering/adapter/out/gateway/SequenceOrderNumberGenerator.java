package com.springbootecommerce.shophappens.ordering.adapter.out.gateway;

import com.springbootecommerce.shophappens.ordering.application.port.out.OrderNumberGenerator;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderNumber;
import java.time.Clock;
import java.time.Year;
import java.util.Locale;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
class SequenceOrderNumberGenerator implements OrderNumberGenerator {
    private final JdbcTemplate jdbc;
    private final Clock clock;

    SequenceOrderNumberGenerator(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Override
    public OrderNumber next() {
        Long sequence = jdbc.queryForObject("SELECT nextval('order_number_sequence')", Long.class);
        if (sequence == null) {
            throw new IllegalStateException("Order number sequence returned no value");
        }
        return new OrderNumber(
                String.format(Locale.ROOT, "ORD-%d-%06d", Year.now(clock).getValue(), sequence));
    }
}
