package com.springbootecommerce.shophappens.account.adapter.in.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.account.application.port.in.AccountReference;
import com.springbootecommerce.shophappens.account.application.port.in.AuthenticateAccountQuery;
import com.springbootecommerce.shophappens.account.application.port.in.AuthenticationAccount;
import com.springbootecommerce.shophappens.account.application.port.in.AuthenticationRole;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class AccountUserDetailsServiceTest {

    @Mock AuthenticateAccountQuery accounts;

    @Test
    void mapsPublishedCustomerAuthenticationToSpringAuthority() {
        when(accounts.findByEmail("customer@example.com"))
                .thenReturn(
                        Optional.of(
                                new AuthenticationAccount(
                                        new AccountReference(42L),
                                        "customer@example.com",
                                        "{bcrypt}hash",
                                        AuthenticationRole.CUSTOMER,
                                        true)));

        var details =
                new AccountUserDetailsService(accounts)
                        .loadUserByUsername(" CUSTOMER@EXAMPLE.COM ");

        assertThat(details.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_CUSTOMER");
        assertThat(details.getUsername()).isEqualTo("customer@example.com");
        verify(accounts).findByEmail("customer@example.com");
    }

    @Test
    void mapsPublishedAdminAuthenticationToSpringAuthority() {
        when(accounts.findByEmail("admin@example.com"))
                .thenReturn(
                        Optional.of(
                                new AuthenticationAccount(
                                        new AccountReference(43L),
                                        "admin@example.com",
                                        "{bcrypt}hash",
                                        AuthenticationRole.ADMIN,
                                        true)));

        var details =
                new AccountUserDetailsService(accounts).loadUserByUsername("admin@example.com");

        assertThat(details.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
        verify(accounts).findByEmail("admin@example.com");
    }

    @Test
    void preservesDisabledAccountEnabledFlag() {
        when(accounts.findByEmail("disabled@example.com"))
                .thenReturn(
                        Optional.of(
                                new AuthenticationAccount(
                                        new AccountReference(44L),
                                        "disabled@example.com",
                                        "{bcrypt}hash",
                                        AuthenticationRole.CUSTOMER,
                                        false)));

        var details =
                new AccountUserDetailsService(accounts).loadUserByUsername("disabled@example.com");

        assertThat(details.isEnabled()).isFalse();
        verify(accounts).findByEmail("disabled@example.com");
    }

    @Test
    void throwsUsernameNotFoundWhenQueryIsEmpty() {
        when(accounts.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                new AccountUserDetailsService(accounts)
                                        .loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
        verify(accounts).findByEmail("missing@example.com");
    }
}
