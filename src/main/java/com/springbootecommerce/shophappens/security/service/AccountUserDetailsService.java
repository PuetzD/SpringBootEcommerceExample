package com.springbootecommerce.shophappens.security.service;

import com.springbootecommerce.shophappens.account.application.AccountAuthenticationQuery;
import com.springbootecommerce.shophappens.account.application.EmailNormalizer;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AccountUserDetailsService implements UserDetailsService {

    private final AccountAuthenticationQuery accountAuthenticationQuery;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String email)
            throws UsernameNotFoundException {
        var account = accountAuthenticationQuery.findByEmail(EmailNormalizer.normalize(email));
        var authority = account.authority();
        if (authority == null) {
            throw new IllegalArgumentException("Account role must not be null");
        }

        return new AuthenticatedAccountPrincipal(
                account.id(),
                account.email(),
                account.passwordHash(),
                account.enabled(),
                authority);
    }
}
