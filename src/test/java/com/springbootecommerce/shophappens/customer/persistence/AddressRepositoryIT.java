package com.springbootecommerce.shophappens.customer.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.shophappens.account.domain.Account;
import com.springbootecommerce.shophappens.account.domain.Role;
import com.springbootecommerce.shophappens.account.persistence.AccountRepository;
import com.springbootecommerce.shophappens.customer.domain.Address;
import com.springbootecommerce.shophappens.customer.domain.Customer;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AddressRepositoryIT extends AbstractIntegrationTest {

    @Autowired AddressRepository addressRepository;
    @Autowired CustomerRepository customerRepository;
    @Autowired AccountRepository accountRepository;

    @Test
    void createsAddressForCustomer() {
        var account = new Account();
        account.setEmail("addr-test@example.com");
        account.setPasswordHash("encoded");
        account.setRole(Role.CUSTOMER);
        account.setEnabled(true);
        accountRepository.saveAndFlush(account);

        var customer = Customer.forAccount(account.getId());
        customerRepository.saveAndFlush(customer);

        var address = new Address();
        address.setCustomer(customer);
        address.setRecipientName("John Doe");
        address.setAddressLine1("123 Main St");
        address.setCity("Berlin");
        address.setPostalCode("10115");
        address.setCountryCode("DE");
        address.setDefaultShipping(true);
        addressRepository.saveAndFlush(address);

        var found = addressRepository.findByCustomerIdOrderByDefaultShippingDesc(customer.getId());
        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getRecipientName()).isEqualTo("John Doe");
    }

    @Test
    void findsDefaultBillingAddress() {
        var account = new Account();
        account.setEmail("billing-test@example.com");
        account.setPasswordHash("encoded");
        account.setRole(Role.CUSTOMER);
        account.setEnabled(true);
        accountRepository.saveAndFlush(account);

        var customer = Customer.forAccount(account.getId());
        customerRepository.saveAndFlush(customer);

        var address = new Address();
        address.setCustomer(customer);
        address.setRecipientName("Jane Smith");
        address.setAddressLine1("456 Oak Ave");
        address.setCity("Munich");
        address.setPostalCode("80331");
        address.setCountryCode("DE");
        address.setDefaultBilling(true);
        addressRepository.saveAndFlush(address);

        var found = addressRepository.findByCustomerIdAndDefaultBillingTrue(customer.getId());
        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getRecipientName()).isEqualTo("Jane Smith");
    }
}
