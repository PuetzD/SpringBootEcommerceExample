package com.springbootecommerce.shophappens.account.adapter.out.persistence;

import com.springbootecommerce.shophappens.account.domain.model.Account;
import com.springbootecommerce.shophappens.account.domain.model.Email;
import com.springbootecommerce.shophappens.account.domain.model.PasswordHash;
import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import org.springframework.stereotype.Component;

@Component
class AccountPersistenceMapper {
    AccountJpaEntity toJpa(Account account) {
        var jpa =
                AccountJpaEntity.create(
                        account.email(), account.passwordHash(), account.role(), account.enabled());
        account.id().ifPresent(id -> jpa.setId(id.value()));
        return jpa;
    }

    Account toDomain(AccountJpaEntity jpa) {
        return Account.restore(
                new AccountId(jpa.getId()),
                new Email(jpa.getEmail()),
                new PasswordHash(jpa.getPasswordHash()),
                jpa.getRole(),
                jpa.isEnabled());
    }
}
