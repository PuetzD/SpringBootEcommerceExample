package com.springbootecommerce.shophappens.sharedkernel.money;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {
    @Test
    void normalizesToTwoDecimalPlaces() {
        assertThat(new Money(new BigDecimal("12.5")).amount()).isEqualByComparingTo("12.50");
    }

    @Test
    void rejectsNegativeAndFractionalCentAmounts() {
        assertThatThrownBy(() -> new Money(new BigDecimal("-0.01")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Money(new BigDecimal("1.001")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addsAndMultiplies() {
        var price = new Money(new BigDecimal("19.99"));
        assertThat(price.add(price)).isEqualTo(new Money(new BigDecimal("39.98")));
        assertThat(price.multiply(3)).isEqualTo(new Money(new BigDecimal("59.97")));
    }

    @Test
    void rejectsNonPositiveMultipliers() {
        var price = new Money(new BigDecimal("1.00"));
        assertThatThrownBy(() -> price.multiply(0)).isInstanceOf(IllegalArgumentException.class);
    }
}
