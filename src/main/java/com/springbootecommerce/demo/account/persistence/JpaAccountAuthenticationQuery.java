package com.springbootecommerce.demo.account.persistence;

import com.springbootecommerce.demo.account.application.AccountAuthenticationQuery;
import com.springbootecommerce.demo.account.application.AuthenticatedAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class JpaAccountAuthenticationQuery implements AccountAuthenticationQuery {
    private final AccountRepository accountRepository;

    @Override
    public AuthenticatedAccount findByEmail(String email) throws UsernameNotFoundException {
        return accountRepository
                .findByEmailIgnoreCase(email)
                .map(
                        account ->
                                new AuthenticatedAccount(
                                        account.getEmail(),
                                        account.getPasswordHash(),
                                        account.getRole(),
                                        account.isEnabled()))
                .orElseThrow(() -> new UsernameNotFoundException("Unknown email: " + email));
    }
}
