package com.springbootecommerce.shophappens.account.adapter.in.security;

import com.springbootecommerce.shophappens.account.application.port.in.AuthenticateAccountQuery;
import com.springbootecommerce.shophappens.account.application.port.in.AuthenticationAccount;
import com.springbootecommerce.shophappens.account.application.port.in.AuthenticationRole;
import com.springbootecommerce.shophappens.account.domain.model.Email;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccountUserDetailsService implements UserDetailsService {
    private final AuthenticateAccountQuery accounts;

    public AccountUserDetailsService(AuthenticateAccountQuery accounts) {
        this.accounts = accounts;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Email email = new Email(username);
        AuthenticationAccount account =
                accounts.findByEmail(email.value())
                        .orElseThrow(
                                () ->
                                        new UsernameNotFoundException(
                                                "No account found for " + email.value()));
        return new AccountPrincipal(
                account.account(),
                account.email(),
                account.passwordHash(),
                account.enabled(),
                authorityFor(account.role()));
    }

    private GrantedAuthority authorityFor(AuthenticationRole role) {
        return new SimpleGrantedAuthority(
                switch (role) {
                    case CUSTOMER -> "ROLE_CUSTOMER";
                    case ADMIN -> "ROLE_ADMIN";
                });
    }
}
