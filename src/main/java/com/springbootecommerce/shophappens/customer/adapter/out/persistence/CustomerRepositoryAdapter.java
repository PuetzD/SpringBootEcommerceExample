package com.springbootecommerce.shophappens.customer.adapter.out.persistence;

import com.springbootecommerce.shophappens.customer.application.port.out.CustomerRepository;
import com.springbootecommerce.shophappens.customer.domain.model.AccountId;
import com.springbootecommerce.shophappens.customer.domain.model.Customer;
import com.springbootecommerce.shophappens.customer.domain.model.CustomerId;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class CustomerRepositoryAdapter implements CustomerRepository {
    private final SpringDataCustomerRepository springData;
    private final CustomerPersistenceMapper mapper;

    CustomerRepositoryAdapter(
            SpringDataCustomerRepository springData, CustomerPersistenceMapper mapper) {
        this.springData = springData;
        this.mapper = mapper;
    }

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
