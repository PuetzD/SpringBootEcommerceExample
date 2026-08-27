package com.springbootecommerce.demo.customer.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.demo.account.domain.Account;
import com.springbootecommerce.demo.account.domain.Role;
import com.springbootecommerce.demo.account.persistence.AccountRepository;
import com.springbootecommerce.demo.customer.domain.Customer;
import com.springbootecommerce.demo.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

class CustomerRepositoryIT extends AbstractIntegrationTest {

    @Autowired CustomerRepository customerRepository;
    @Autowired AccountRepository accountRepository;

    @Test
    @Transactional
    void createsCustomerFromAccount() {
        var account = new Account();
        account.setEmail("test@example.com");
        account.setPasswordHash("encoded");
        account.setRole(Role.CUSTOMER);
        account.setEnabled(true);
        accountRepository.saveAndFlush(account);

        var customer = new Customer();
        customer.setAccount(account);
        customerRepository.saveAndFlush(customer);

        var found = customerRepository.findById(account.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getAccount().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findsCustomerByEmail() {
        var account = new Account();
        account.setEmail("find-me@example.com");
        account.setPasswordHash("encoded");
        account.setRole(Role.CUSTOMER);
        account.setEnabled(true);
        accountRepository.saveAndFlush(account);

        var customer = new Customer();
        customer.setAccount(account);
        customerRepository.saveAndFlush(customer);

        var found = customerRepository.findByAccountEmailIgnoreCase("FIND-ME@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(account.getId());
    }
}
