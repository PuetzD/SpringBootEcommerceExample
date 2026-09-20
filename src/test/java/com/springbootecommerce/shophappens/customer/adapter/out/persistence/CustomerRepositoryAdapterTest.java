package com.springbootecommerce.shophappens.customer.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.customer.application.port.in.CustomerProfileAlreadyExistsException;
import com.springbootecommerce.shophappens.customer.domain.model.ContactEmail;
import com.springbootecommerce.shophappens.customer.domain.model.Customer;
import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class CustomerRepositoryAdapterTest {
    private final SpringDataCustomerRepository springData =
            mock(SpringDataCustomerRepository.class);
    private final CustomerPersistenceMapper mapper = mock(CustomerPersistenceMapper.class);
    private final CustomerRepositoryAdapter adapter =
            new CustomerRepositoryAdapter(springData, mapper);

    @Test
    void propagatesUnrelatedIntegrityFailuresUnchanged() {
        var customer =
                Customer.create(
                        new AccountId(42L), "Ada", "Lovelace", new ContactEmail("ada@example.com"));
        var entity = mock(CustomerJpaEntity.class);
        var failure = new DataIntegrityViolationException("different constraint");
        when(mapper.toJpa(customer)).thenReturn(entity);
        when(springData.saveAndFlush(entity)).thenThrow(failure);

        assertThatThrownBy(() -> adapter.save(customer)).isSameAs(failure);
    }

    @Test
    void translatesCustomerAccountConstraintFailures() {
        var customer =
                Customer.create(
                        new AccountId(42L), "Ada", "Lovelace", new ContactEmail("ada@example.com"));
        var entity = mock(CustomerJpaEntity.class);
        var violation =
                new org.hibernate.exception.ConstraintViolationException(
                        "duplicate", null, "uk_customer_account");
        var failure = new DataIntegrityViolationException("duplicate", violation);
        when(mapper.toJpa(customer)).thenReturn(entity);
        when(springData.saveAndFlush(entity)).thenThrow(failure);

        assertThatThrownBy(() -> adapter.save(customer))
                .isInstanceOf(CustomerProfileAlreadyExistsException.class);
    }
}
