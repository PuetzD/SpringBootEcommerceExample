package com.springbootecommerce.shophappens.cart.adapter.out.persistence;

import com.springbootecommerce.shophappens.cart.application.port.out.GuestCartWriteGuard;
import com.springbootecommerce.shophappens.cart.domain.model.GuestCartId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
class PostgresGuestCartWriteGuard implements GuestCartWriteGuard {
    private final JdbcTemplate jdbc;
    private final String lockTimeout;

    PostgresGuestCartWriteGuard(
            JdbcTemplate jdbc, @Value("${cart.guest.lock-timeout:2s}") String lockTimeout) {
        this.jdbc = jdbc;
        this.lockTimeout = lockTimeout;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void acquire(GuestCartId id) {
        jdbc.queryForObject(
                "select set_config('lock_timeout', ?, true)", String.class, lockTimeout);
        jdbc.queryForObject(
                "select pg_advisory_xact_lock(hashtextextended(?, 0))",
                Object.class,
                "guest-cart:" + id.value());
    }
}
