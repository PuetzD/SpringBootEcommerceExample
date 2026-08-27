package com.springbootecommerce.demo.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.springbootecommerce.demo.account.application.AccountAuthenticationQuery;
import com.springbootecommerce.demo.account.application.AuthenticatedAccount;
import com.springbootecommerce.demo.account.domain.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
public class AccountUserDetailsServiceTest {

    @Mock private AccountAuthenticationQuery accountQuery;

    @Test
    void mapsCustomerToCustomerAuthority() {
        var account =
                new AuthenticatedAccount(
                        42L, "customer@example.com", "{bcrypt}password-hash", Role.CUSTOMER, true);
        when(accountQuery.findByEmail("customer@example.com")).thenReturn(account);

        var userDetails =
                new AccountUserDetailsService(accountQuery)
                        .loadUserByUsername(" CUSTOMER@EXAMPLE.COM ");

        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_CUSTOMER");
        assertThat(userDetails).isInstanceOf(AuthenticatedAccountPrincipal.class);
        assertThat(((AuthenticatedAccountPrincipal) userDetails).accountId()).isEqualTo(42L);
        verify(accountQuery).findByEmail("customer@example.com");
    }

    @Test
    void mapsAdminToAdminAuthority() {
        var account =
                new AuthenticatedAccount(
                        43L, "admin@example.com", "{bcrypt}password-hash", Role.ADMIN, true);
        when(accountQuery.findByEmail("admin@example.com")).thenReturn(account);

        var adminDetails =
                new AccountUserDetailsService(accountQuery).loadUserByUsername("admin@example.com");

        assertThat(adminDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
        assertThat(adminDetails).isInstanceOf(AuthenticatedAccountPrincipal.class);
        assertThat(((AuthenticatedAccountPrincipal) adminDetails).accountId()).isEqualTo(43L);
        verify(accountQuery).findByEmail("admin@example.com");
    }

    @Test
    void preservesDisabledAccount() {
        var account =
                new AuthenticatedAccount(
                        44L, "disabled@example.com", "{bcrypt}password-hash", Role.CUSTOMER, false);
        when(accountQuery.findByEmail("disabled@example.com")).thenReturn(account);

        var userDetails =
                new AccountUserDetailsService(accountQuery)
                        .loadUserByUsername("disabled@example.com");

        assertThat(userDetails.isEnabled()).isFalse();
        verify(accountQuery).findByEmail("disabled@example.com");
    }

    @Test
    void propagatesUnknownEmail() {
        var exception = new UsernameNotFoundException("Unknown email");
        when(accountQuery.findByEmail("unknown@example.com")).thenThrow(exception);

        assertThatThrownBy(
                        () ->
                                new AccountUserDetailsService(accountQuery)
                                        .loadUserByUsername("unknown@example.com"))
                .isSameAs(exception);
        verify(accountQuery).findByEmail("unknown@example.com");
    }

    @Test
    void rejectsAccountWithoutRole() {
        var account =
                new AuthenticatedAccount(
                        45L, "missing-role@example.com", "{bcrypt}password-hash", null, true);
        when(accountQuery.findByEmail("missing-role@example.com")).thenReturn(account);

        assertThatThrownBy(
                        () ->
                                new AccountUserDetailsService(accountQuery)
                                        .loadUserByUsername("missing-role@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Account role must not be null");
        verify(accountQuery).findByEmail("missing-role@example.com");
    }
}
