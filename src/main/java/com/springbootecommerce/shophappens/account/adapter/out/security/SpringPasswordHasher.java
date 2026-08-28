package com.springbootecommerce.shophappens.account.adapter.out.security;

import com.springbootecommerce.shophappens.account.application.port.out.PasswordHasher;
import com.springbootecommerce.shophappens.account.domain.model.PasswordHash;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
final class SpringPasswordHasher implements PasswordHasher {
    private final PasswordEncoder encoder;

    SpringPasswordHasher(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public PasswordHash hash(String rawPassword) {
        return new PasswordHash(encoder.encode(rawPassword));
    }
}
