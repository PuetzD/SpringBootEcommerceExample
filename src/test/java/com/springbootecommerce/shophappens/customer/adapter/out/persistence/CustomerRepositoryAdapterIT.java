package com.springbootecommerce.shophappens.customer.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdminSearch;
import com.springbootecommerce.shophappens.customer.application.port.out.CustomerRepository;
import com.springbootecommerce.shophappens.customer.domain.model.AddressDetails;
import com.springbootecommerce.shophappens.customer.domain.model.Customer;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
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

        Customer unsaved =
                Customer.create(
                        new AccountId(accountId),
                        "Ada",
                        "Lovelace",
                        new com.springbootecommerce.shophappens.customer.domain.model.ContactEmail(
                                "ada@example.com"));
        unsaved.addAddress(testCityAddress(), true, true);

        Customer saved = customers.save(unsaved);
        Customer restored = customers.findById(saved.id().orElseThrow()).orElseThrow();

        assertThat(restored.accountId()).isEqualTo(new AccountId(accountId));
        assertThat(restored.givenName()).isEqualTo("Ada");
        assertThat(restored.familyName()).isEqualTo("Lovelace");
        assertThat(restored.contactEmail().value()).isEqualTo("ada@example.com");
        assertThat(restored.addresses())
                .singleElement()
                .satisfies(address -> assertThat(address.defaultShipping()).isTrue());
    }

    @Test
    void databaseRejectsTwoShippingDefaultsForOneCustomer() {
        Long accountId = newAccount("adapter-default-fixture@example.com");
        Long customerId =
                jdbc.queryForObject(
                        """
                        insert into customer (account_id, given_name, family_name, contact_email)
                        values (?, ?, ?, ?) returning id
                        """,
                        Long.class,
                        accountId,
                        "Ada",
                        "Lovelace",
                        "ada@example.com");
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
                "Greymoor",
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
                                        "Greymoor",
                                        "35037",
                                        "DE"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void searchesCustomerOwnedNamesAndContactEmailCaseInsensitively() {
        saveCustomer("Alice", "Example", "first@example.com");
        saveCustomer("Bob", "Smith", "second@example.com");
        saveCustomer("Carol", "Jones", "Contact.Match@example.com");

        assertThat(customers.searchForAdministration(new CustomerAdminSearch(0, 20, "LIC")))
                .extracting("content")
                .asList()
                .singleElement()
                .satisfies(
                        summary ->
                                assertThat(summary)
                                        .hasFieldOrPropertyWithValue("givenName", "Alice"));
        assertThat(customers.searchForAdministration(new CustomerAdminSearch(0, 20, "mit")))
                .extracting("content")
                .asList()
                .singleElement()
                .satisfies(
                        summary ->
                                assertThat(summary)
                                        .hasFieldOrPropertyWithValue("familyName", "Smith"));
        assertThat(
                        customers.searchForAdministration(
                                new CustomerAdminSearch(0, 20, "MATCH@EXAMPLE")))
                .extracting("content")
                .asList()
                .singleElement()
                .satisfies(
                        summary ->
                                assertThat(summary)
                                        .hasFieldOrPropertyWithValue(
                                                "contactEmail", "contact.match@example.com"));
    }

    @Test
    void searchesWithPaginationAndNewestCustomerIdFirst() {
        var first = saveCustomer("First", "Customer", "first-page@example.com");
        var second = saveCustomer("Second", "Customer", "second-page@example.com");
        var third = saveCustomer("Third", "Customer", "third-page@example.com");

        var page = customers.searchForAdministration(new CustomerAdminSearch(0, 2, null));

        assertThat(page.content())
                .extracting(summary -> summary.customerId().value())
                .containsExactly(third, second);
        assertThat(page.page()).isZero();
        assertThat(page.size()).isEqualTo(2);
        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.totalPages()).isEqualTo(2);
        assertThat(first).isLessThan(second).isLessThan(third);
    }

    @Test
    void returnsEmptyPageWhenNoCustomerMatches() {
        var page =
                customers.searchForAdministration(new CustomerAdminSearch(0, 20, "does-not-exist"));

        assertThat(page.content()).isEmpty();
        assertThat(page.totalElements()).isZero();
    }

    @Test
    void treatsLikeWildcardsAsLiteralSearchText() {
        saveCustomer("Percent%Name", "Example", "literal@example.com");
        saveCustomer("Plain", "Example", "other@example.com");

        var page = customers.searchForAdministration(new CustomerAdminSearch(0, 20, "%"));

        assertThat(page.content()).extracting("givenName").containsExactly("Percent%Name");
    }

    @Test
    void mapsCustomerDetailAndAddressesWithoutOrderingData() {
        var accountId = newAccount("detail-account@example.com");
        var customer =
                Customer.create(
                        new AccountId(accountId),
                        "Ada",
                        "Lovelace",
                        new com.springbootecommerce.shophappens.customer.domain.model.ContactEmail(
                                "contact@example.com"));
        customer.addAddress(testCityAddress(), true, false);
        var saved = customers.save(customer);

        var detail = customers.findForAdministration(saved.id().orElseThrow()).orElseThrow();

        assertThat(detail.customerId()).isEqualTo(saved.id().orElseThrow());
        assertThat(detail.accountId()).isEqualTo(new AccountId(accountId));
        assertThat(detail.givenName()).isEqualTo("Ada");
        assertThat(detail.familyName()).isEqualTo("Lovelace");
        assertThat(detail.contactEmail()).isEqualTo("contact@example.com");
        assertThat(detail.addresses())
                .singleElement()
                .satisfies(
                        address -> {
                            assertThat(address.recipientName())
                                    .isEqualTo("Bard the Magnificent Debugger");
                            assertThat(address.city()).isEqualTo("Greymoor");
                            assertThat(address.defaultShipping()).isTrue();
                            assertThat(address.defaultBilling()).isFalse();
                        });
    }

    @Test
    void returnsEmptyForUnknownCustomerId() {
        assertThat(customers.findForAdministration(new CustomerId(Long.MAX_VALUE))).isEmpty();
    }

    private Long saveCustomer(String givenName, String familyName, String contactEmail) {
        var customer =
                Customer.create(
                        new AccountId(
                                newAccount(
                                        "account-"
                                                + givenName.toLowerCase()
                                                + "-"
                                                + familyName.toLowerCase()
                                                + "@example.com")),
                        givenName,
                        familyName,
                        new com.springbootecommerce.shophappens.customer.domain.model.ContactEmail(
                                contactEmail));
        return customers.save(customer).id().orElseThrow().value();
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
                "Bard the Magnificent Debugger",
                null,
                "1 Main Street",
                null,
                "Greymoor",
                null,
                "35037",
                "DE",
                null);
    }
}
