package com.springbootecommerce.shophappens.account.persistence;

import com.springbootecommerce.shophappens.account.application.AccountAuthenticationQuery;
import com.springbootecommerce.shophappens.account.application.AuthenticatedAccount;
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
                                        account.getId(),
                                        account.getEmail(),
                                        account.getPasswordHash(),
                                        account.getRole() == null
                                                ? null
                                                : account.getRole().authority(),
                                        account.isEnabled()))
                .orElseThrow(() -> new UsernameNotFoundException("Unknown email: " + email));
    }
}
