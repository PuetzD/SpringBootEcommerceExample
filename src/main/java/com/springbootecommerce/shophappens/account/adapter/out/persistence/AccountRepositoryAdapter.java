package com.springbootecommerce.shophappens.account.adapter.out.persistence;

import com.springbootecommerce.shophappens.account.application.port.out.AccountRepository;
import com.springbootecommerce.shophappens.account.domain.model.Account;
import com.springbootecommerce.shophappens.account.domain.model.Email;
import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class AccountRepositoryAdapter implements AccountRepository {
    private final SpringDataAccountRepository springData;
    private final AccountPersistenceMapper mapper;

    @Override
    public boolean existsByEmail(Email email) {
        return springData.existsByEmail(email.value());
    }

    @Override
    public Optional<Account> findByEmail(Email email) {
        return springData.findByEmail(email.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findById(AccountId id) {
        return springData.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Account save(Account account) {
        return mapper.toDomain(springData.save(mapper.toJpa(account)));
    }

    @Override
    public void delete(AccountId id) {
        springData.deleteById(id.value());
    }
}
