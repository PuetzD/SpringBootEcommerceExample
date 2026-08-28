package com.springbootecommerce.shophappens.customer.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.springbootecommerce.shophappens.customer.application.port.out.CustomerRepository;
import com.springbootecommerce.shophappens.customer.domain.model.AccountId;
import com.springbootecommerce.shophappens.customer.domain.model.AddressDetails;
import com.springbootecommerce.shophappens.customer.domain.model.Customer;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

class CustomerRepositoryAdapterIT extends AbstractIntegrationTest {
    @Autowired CustomerRepository customers;
    @Autowired JdbcTemplate jdbc;

    @Test
    void savesAndRestoresTheWholeAggregate() {
        Long accountId = newAccount("adapter-fixture@example.com");

        Customer unsaved = Customer.create(new AccountId(accountId));
        unsaved.addAddress(testCityAddress(), true, true);

        Customer saved = customers.save(unsaved);
        Customer restored = customers.findById(saved.id().orElseThrow()).orElseThrow();

        assertThat(restored.accountId()).isEqualTo(new AccountId(accountId));
        assertThat(restored.addresses())
                .singleElement()
                .satisfies(address -> assertThat(address.defaultShipping()).isTrue());
    }

    @Test
    void databaseRejectsTwoShippingDefaultsForOneCustomer() {
        Long accountId = newAccount("adapter-default-fixture@example.com");
        Long customerId =
                jdbc.queryForObject(
                        "insert into customer (account_id) values (?) returning id",
                        Long.class,
                        accountId);
        jdbc.update(
                """
                insert into address(
                    customer_id, recipient_name, address_line_1, city,
                    postal_code, country_code, is_default_shipping, is_default_billing)
                values (?, ?, ?, ?, ?, ?, true, false)
                """,
                customerId,
                "First",
                "1 Main Street",
                "Testcity",
                "35037",
                "DE");
        String insert =
                """
                insert into address(
                    customer_id, recipient_name, address_line_1, city,
                    postal_code, country_code, is_default_shipping, is_default_billing)
                values (?, ?, ?, ?, ?, ?, true, false)
                """;

        assertThatThrownBy(
                        () ->
                                jdbc.update(
                                        insert,
                                        customerId,
                                        "Second",
                                        "2 Main Street",
                                        "Testcity",
                                        "35037",
                                        "DE"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Long newAccount(String email) {
        return jdbc.queryForObject(
                "insert into account (email, password_hash, role) values (?, ?, ?) returning id",
                Long.class,
                email,
                "encoded",
                "CUSTOMER");
    }

    private AddressDetails testCityAddress() {
        return new AddressDetails(
                "Alex Example", null, "1 Main Street", null, "Testcity", null, "35037", "DE", null);
    }
}
