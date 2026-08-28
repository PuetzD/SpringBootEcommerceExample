package com.springbootecommerce.shophappens.account.application.port.out;

import com.springbootecommerce.shophappens.account.domain.model.Account;
import com.springbootecommerce.shophappens.account.domain.model.Email;
import java.util.Optional;

public interface AccountRepository {
    boolean existsByEmail(Email email);

    Optional<Account> findByEmail(Email email);

    Account save(Account account);
}
