package com.springbootecommerce.shophappens.account.adapter.in.security;

import com.springbootecommerce.shophappens.account.application.port.in.AccountReference;
import com.springbootecommerce.shophappens.account.application.port.in.AuthenticatedAccountIdentity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public final class CurrentAccountIdentity implements AuthenticatedAccountIdentity {

    @Override
    public AccountReference account() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !(authentication.getPrincipal()
                        instanceof AuthenticatedAccountIdentity identity)) {
            throw new IllegalStateException("No authenticated account identity is available");
        }
        return identity.account();
    }
}
