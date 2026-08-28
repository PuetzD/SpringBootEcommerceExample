package com.springbootecommerce.shophappens.account.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AccountTest {
    @Test
    void registersAnEnabledCustomerWithCanonicalEmail() {
        Account account =
                Account.registerCustomer(
                        new Email(" Customer@Example.COM "), new PasswordHash("{bcrypt}encoded"));

        assertThat(account.email()).isEqualTo(new Email("customer@example.com"));
        assertThat(account.role()).isEqualTo(Role.CUSTOMER);
        assertThat(account.enabled()).isTrue();
        assertThat(account.id()).isEmpty();
    }

    @Test
    void controlsEnabledStateWithoutSetters() {
        Account account =
                Account.registerCustomer(
                        new Email("customer@example.com"), new PasswordHash("{bcrypt}encoded"));
        account.disable();
        assertThat(account.enabled()).isFalse();
        account.enable();
        assertThat(account.enabled()).isTrue();
    }
}
