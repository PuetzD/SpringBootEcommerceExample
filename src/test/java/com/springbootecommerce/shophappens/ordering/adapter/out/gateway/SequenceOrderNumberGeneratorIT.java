package com.springbootecommerce.shophappens.ordering.adapter.out.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.ordering.application.port.out.OrderNumberGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SequenceOrderNumberGeneratorIT extends AbstractIntegrationTest {
    @Autowired OrderNumberGenerator numbers;

    @Test
    void allocatesDistinctNumbersFromTheBaselineSequence() {
        String first = numbers.next().value();
        String second = numbers.next().value();

        assertThat(first).matches("ORD-\\d{4}-\\d{6,}");
        assertThat(second).matches("ORD-\\d{4}-\\d{6,}").isNotEqualTo(first);
    }
}
