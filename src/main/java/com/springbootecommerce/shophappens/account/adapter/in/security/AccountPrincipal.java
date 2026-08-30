package com.springbootecommerce.shophappens.account.adapter.in.security;

import com.springbootecommerce.shophappens.account.application.port.in.AccountReference;
import com.springbootecommerce.shophappens.account.application.port.in.AuthenticatedAccountIdentity;
import java.io.Serializable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@RequiredArgsConstructor
public final class AccountPrincipal
        implements UserDetails, AuthenticatedAccountIdentity, Serializable {
    private final AccountReference account;
    private final String email;
    private final String passwordHash;
    private final boolean enabled;
    private final GrantedAuthority authority;

    @Override
    public AccountReference account() {
        return account;
    }

    @Override
    public List<? extends GrantedAuthority> getAuthorities() {
        return List.of(authority);
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
