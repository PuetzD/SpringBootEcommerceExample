package com.springbootecommerce.shophappens.cart.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class QuantityTest {

    @Test
    void quantityMustBePositive() {
        assertThat(new Quantity(2).value()).isEqualTo(2);
        assertThatThrownBy(() -> new Quantity(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Quantity(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}
