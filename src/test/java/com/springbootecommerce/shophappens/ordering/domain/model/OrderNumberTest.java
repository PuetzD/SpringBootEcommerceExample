package com.springbootecommerce.shophappens.ordering.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class OrderNumberTest {
    @Test
    void acceptsSequenceBasedNumbersAndValuesLongerThanSixDigits() {
        assertThat(new OrderNumber("ORD-2026-100001").value()).isEqualTo("ORD-2026-100001");
        assertThat(new OrderNumber("ORD-2026-1000000").value()).isEqualTo("ORD-2026-1000000");
    }

    @Test
    void rejectsThePreviousRandomSuffixFormat() {
        assertThatThrownBy(() -> new OrderNumber("ORD-20260828-ABC123DEF456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order number format is invalid");
    }

    @Test
    void rejectsMissingPrefixAndShortSequenceValues() {
        assertThatThrownBy(() -> new OrderNumber("2026-100001"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OrderNumber("ORD-2026-00001"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
