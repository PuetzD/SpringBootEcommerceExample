package com.springbootecommerce.demo.security.service;

import com.springbootecommerce.demo.account.application.AccountAuthenticationQuery;
import com.springbootecommerce.demo.account.application.EmailNormalizer;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AccountUserDetailsService implements UserDetailsService {

  private final AccountAuthenticationQuery accountAuthenticationQuery;

  @Override
  public @NonNull UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
    var account = accountAuthenticationQuery.findByEmail(EmailNormalizer.normalize(email));
    var role = account.role();
    if (role == null) {
      throw new IllegalArgumentException("Account role must not be null");
    }

    var authority =
        switch (role) {
          case CUSTOMER -> "ROLE_CUSTOMER";
          case ADMIN -> "ROLE_ADMIN";
        };

    return User.withUsername(account.email())
        .password(account.passwordHash())
        .authorities(authority)
        .disabled(!account.enabled())
        .build();
  }
}
