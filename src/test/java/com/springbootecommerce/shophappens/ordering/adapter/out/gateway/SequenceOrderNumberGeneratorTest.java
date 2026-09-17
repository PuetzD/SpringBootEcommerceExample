package com.springbootecommerce.shophappens.ordering.adapter.out.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class SequenceOrderNumberGeneratorTest {
    @Test
    void combinesTheUtcYearWithThePaddedSequenceValue() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject("SELECT nextval('order_number_sequence')", Long.class))
                .thenReturn(100_001L);
        var generator =
                new SequenceOrderNumberGenerator(
                        jdbc, Clock.fixed(Instant.parse("2026-12-31T23:59:59Z"), ZoneOffset.UTC));

        assertThat(generator.next().value()).isEqualTo("ORD-2026-100001");
    }
}
