package com.springbootecommerce.shophappens.sharedkernel.identity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ProductVariantIdTest {

    @Test
    void rejectsNonPositiveValues() {
        assertThatThrownBy(() -> new ProductVariantId(0L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
