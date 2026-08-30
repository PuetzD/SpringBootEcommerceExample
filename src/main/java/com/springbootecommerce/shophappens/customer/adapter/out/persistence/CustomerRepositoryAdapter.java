package com.springbootecommerce.shophappens.customer.adapter.out.persistence;

import com.springbootecommerce.shophappens.customer.application.port.out.CustomerRepository;
import com.springbootecommerce.shophappens.customer.domain.model.Customer;
import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class CustomerRepositoryAdapter implements CustomerRepository {
    private final SpringDataCustomerRepository springData;
    private final CustomerPersistenceMapper mapper;

    @Override
    public Optional<Customer> findById(CustomerId id) {
        return springData.findDetailedById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByAccountId(AccountId id) {
        return springData.findByAccountId(id.value()).map(mapper::toDomain);
    }

    @Override
    public Customer save(Customer customer) {
        return mapper.toDomain(springData.save(mapper.toJpa(customer)));
    }
}
