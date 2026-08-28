package com.springbootecommerce.shophappens.account.adapter.out.persistence;

import com.springbootecommerce.shophappens.account.application.port.out.AccountRepository;
import com.springbootecommerce.shophappens.account.domain.model.Account;
import com.springbootecommerce.shophappens.account.domain.model.Email;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class AccountRepositoryAdapter implements AccountRepository {
    private final SpringDataAccountRepository springData;
    private final AccountPersistenceMapper mapper;

    AccountRepositoryAdapter(
            SpringDataAccountRepository springData, AccountPersistenceMapper mapper) {
        this.springData = springData;
        this.mapper = mapper;
    }

    @Override
    public boolean existsByEmail(Email email) {
        return springData.existsByEmail(email.value());
    }

    @Override
    public Optional<Account> findByEmail(Email email) {
        return springData.findByEmail(email.value()).map(mapper::toDomain);
    }

    @Override
    public Account save(Account account) {
        return mapper.toDomain(springData.save(mapper.toJpa(account)));
    }
}
