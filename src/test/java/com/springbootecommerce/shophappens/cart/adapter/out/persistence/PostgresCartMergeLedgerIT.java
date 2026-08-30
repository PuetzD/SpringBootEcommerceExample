package com.springbootecommerce.shophappens.cart.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.shophappens.cart.application.port.out.CartMergeLedger;
import com.springbootecommerce.shophappens.cart.domain.model.GuestCartId;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PostgresCartMergeLedgerIT extends AbstractIntegrationTest {
    @Autowired CartMergeLedger ledger;

    @Test
    void claimsEachGuestCartOnlyOnce() {
        GuestCartId guest = GuestCartId.random();

        assertThat(ledger.claim(guest, new CustomerId(42L))).isTrue();
        assertThat(ledger.claim(guest, new CustomerId(42L))).isFalse();
    }
}
