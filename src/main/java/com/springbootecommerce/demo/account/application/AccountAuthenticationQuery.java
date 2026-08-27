package com.springbootecommerce.demo.account.application;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface AccountAuthenticationQuery {
    AuthenticatedAccount findByEmail(String email) throws UsernameNotFoundException;
}
