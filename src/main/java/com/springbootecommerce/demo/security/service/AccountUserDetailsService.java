package com.springbootecommerce.demo.security.service;

import com.springbootecommerce.demo.account.application.AccountAuthenticationQuery;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RequiredArgsConstructor
public class AccountUserDetailsService implements UserDetailsService {

  private final AccountAuthenticationQuery accountAuthenticationQuery;

  @Override
  public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
    var account = accountAuthenticationQuery.findByEmail(email);
    var role = account.role();
    if (role == null) {
      throw new IllegalArgumentException("Account role must not be null");
    }

    var authority =
        switch (role) {
          case CUSTOMER -> "ROLE_CUSTOMER";
          case ADMIN -> "ROLE_ADMIN";
        };

    return org.springframework.security.core.userdetails.User.withUsername(account.email())
        .password(account.passwordHash())
        .authorities(authority)
        .disabled(!account.enabled())
        .build();
  }
}
