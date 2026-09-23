package com.springbootecommerce.shophappens.account.application.port.out;

import com.springbootecommerce.shophappens.account.domain.model.Account;
import com.springbootecommerce.shophappens.account.domain.model.Email;
import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import java.util.Optional;

public interface AccountRepository {
    boolean existsByEmail(Email email);

    Optional<Account> findByEmail(Email email);

    Optional<Account> findById(AccountId id);

    Account save(Account account);

    void delete(AccountId id);
}
