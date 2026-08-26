package com.springbootecommerce.demo.account.application;

import com.springbootecommerce.demo.account.persistence.AccountRepository;
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
        .orElseThrow(() -> new UsernameNotFoundException("Unknown email: " + email));
  }
}
