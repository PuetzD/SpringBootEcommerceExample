package com.springbootecommerce.shophappens.account.application.port.in;

import java.util.Optional;

public interface AuthenticateAccountQuery {
    Optional<AuthenticationAccount> findByEmail(String email);
}
