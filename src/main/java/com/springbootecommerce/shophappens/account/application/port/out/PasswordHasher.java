package com.springbootecommerce.shophappens.account.application.port.out;

import com.springbootecommerce.shophappens.account.domain.model.PasswordHash;

public interface PasswordHasher {
    PasswordHash hash(String rawPassword);
}
