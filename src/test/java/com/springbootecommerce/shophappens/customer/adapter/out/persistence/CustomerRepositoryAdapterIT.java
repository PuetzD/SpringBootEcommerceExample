package com.springbootecommerce.shophappens.customer.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdminSearch;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerProfileAlreadyExistsException;
import com.springbootecommerce.shophappens.customer.application.port.out.CustomerRepository;
import com.springbootecommerce.shophappens.customer.domain.model.Address;
import com.springbootecommerce.shophappens.customer.domain.model.AddressDetails;
import com.springbootecommerce.shophappens.customer.domain.model.AddressId;
import com.springbootecommerce.shophappens.customer.domain.model.ContactEmail;
import com.springbootecommerce.shophappens.customer.domain.model.Customer;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

class CustomerRepositoryAdapterIT extends AbstractIntegrationTest {
    @Autowired CustomerRepository customers;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactions;

    @Test
    @Sql(
            statements = "delete from account where email = 'duplicate-profile@example.com'",
            executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void translatesDuplicateAccountProfileConstraint() {
        long account = newAccount("duplicate-profile@example.com");
        inTransaction(
                () ->
                        customers.save(
                                Customer.create(
                                        new AccountId(account),
                                        "Ada",
                                        "Lovelace",
                                        new ContactEmail("duplicate-profile@example.com"))));

        assertThatThrownBy(
                        () ->
                                inTransaction(
                                        () ->
                                                customers.save(
                                                        Customer.create(
                                                                new AccountId(account),
                                                                "Ada",
                                                                "Lovelace",
                                                                new ContactEmail(
                                                                        "duplicate-profile@example.com")))))
                .isInstanceOf(CustomerProfileAlreadyExistsException.class);
    }

    @Test
    @Sql(
            statements = "delete from account where email = 'repeat-edit@example.com'",
            executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void editsAnAddressAcrossMultipleCommittedTransactions() {
        long account = newAccount("repeat-edit@example.com");
        var saved =
                inTransaction(
                        () -> {
                            var customer =
                                    Customer.create(
                                            new AccountId(account),
                                            "Ada",
                                            "Lovelace",
                                            new ContactEmail("repeat-edit@example.com"));
                            customer.addAddress(testCityAddress(), true, true);
                            return customers.save(customer);
                        });
        var id = saved.id().orElseThrow();
        var addressId = saved.addresses().getFirst().id().orElseThrow();

        for (String city : java.util.List.of("Berlin", "Hamburg", "Bremen")) {
            inTransaction(
                    () -> {
                        var customer = customers.findById(id).orElseThrow();
                        var old = customer.address(addressId).details();
                        var replacement =
                                new AddressDetails(
                                        old.recipientName(),
                                        old.companyName(),
                                        old.addressLine1(),
                                        old.addressLine2(),
                                        city,
                                        old.region(),
                                        old.postalCode(),
                                        old.countryCode(),
                                        old.phoneNumber());
                        customer.updateAddress(addressId, replacement, true, true);
                        return customers.save(customer);
                    });
            var actual = inTransaction(() -> customers.findById(id).orElseThrow());
            assertThat(actual.address(addressId).details().city()).isEqualTo(city);
            assertThat(actual.addresses()).hasSize(1);
        }
        assertThat(
                        jdbc.queryForObject(
                                "select version from address where id = ?",
                                Long.class,
                                addressId.value()))
                .isGreaterThan(0);
    }

    @Test
    @Sql(
            statements = "delete from account where email = 'switch-defaults@example.com'",
            executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void switchesShippingAndBillingDefaultsBetweenExistingAddresses() {
        long account = newAccount("switch-defaults@example.com");
        var saved =
                inTransaction(
                        () -> {
                            var customer =
                                    Customer.create(
                                            new AccountId(account),
                                            "Ada",
                                            "Lovelace",
                                            new ContactEmail("switch-defaults@example.com"));
                            customer.addAddress(testCityAddress(), false, false);
                            customer.addAddress(testCountryAddress(), true, true);
                            return customers.save(customer);
                        });
        var customerId = saved.id().orElseThrow();
        var firstAddressId = saved.addresses().getFirst().id().orElseThrow();

        inTransaction(
                () -> {
                    var customer = customers.findById(customerId).orElseThrow();
                    var firstAddress = customer.address(firstAddressId);
                    customer.updateAddress(firstAddressId, firstAddress.details(), true, true);
                    return customers.save(customer);
                });

        var actual = inTransaction(() -> customers.findById(customerId).orElseThrow());
        assertThat(actual.address(firstAddressId).defaultShipping()).isTrue();
        assertThat(actual.address(firstAddressId).defaultBilling()).isTrue();
        assertThat(actual.addresses()).filteredOn(address -> address.defaultShipping()).hasSize(1);
        assertThat(actual.addresses()).filteredOn(address -> address.defaultBilling()).hasSize(1);
    }

    @Test
    @Sql(
            statements = "delete from account where email = 'sibling-change@example.com'",
            executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void retainsAnEditedAddressWhileAddingAndRemovingASibling() {
        long account = newAccount("sibling-change@example.com");
        var saved =
                inTransaction(
                        () -> {
                            var customer =
                                    Customer.create(
                                            new AccountId(account),
                                            "Ada",
                                            "Lovelace",
                                            new ContactEmail("sibling-change@example.com"));
                            customer.addAddress(testCityAddress(), true, true);
                            return customers.save(customer);
                        });
        var customerId = saved.id().orElseThrow();
        var editedAddressId = saved.addresses().getFirst().id().orElseThrow();

        var withSibling =
                inTransaction(
                        () -> {
                            var customer = customers.findById(customerId).orElseThrow();
                            customer.updateAddress(
                                    editedAddressId,
                                    withCity(customer.address(editedAddressId).details(), "Berlin"),
                                    true,
                                    true);
                            customer.addAddress(testCountryAddress(), false, false);
                            return customers.save(customer);
                        });
        var siblingId = withSibling.addresses().getLast().id().orElseThrow();

        inTransaction(
                () -> {
                    var customer = customers.findById(customerId).orElseThrow();
                    customer.removeAddress(siblingId);
                    return customers.save(customer);
                });

        var actual = inTransaction(() -> customers.findById(customerId).orElseThrow());
        assertThat(actual.addresses()).hasSize(1);
        assertThat(actual.address(editedAddressId).details().city()).isEqualTo("Berlin");
    }

    @Test
    @Sql(
            statements = "delete from account where email = 'rollback-defaults@example.com'",
            executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void rollsBackTemporaryDefaultClearingWhenFinalUpdateFails() {
        long account = newAccount("rollback-defaults@example.com");
        var saved =
                inTransaction(
                        () -> {
                            var customer =
                                    Customer.create(
                                            new AccountId(account),
                                            "Ada",
                                            "Lovelace",
                                            new ContactEmail("rollback-defaults@example.com"));
                            customer.addAddress(testCityAddress(), true, true);
                            return customers.save(customer);
                        });
        var customerId = saved.id().orElseThrow();
        var originalDefaultId = saved.addresses().getFirst().id().orElseThrow();
        var invalid =
                Customer.restore(
                        customerId,
                        new AccountId(account),
                        "Ada",
                        "Lovelace",
                        new ContactEmail("rollback-defaults@example.com"),
                        java.util.List.of(
                                Address.restore(
                                        new AddressId(Long.MAX_VALUE),
                                        testCountryAddress(),
                                        true,
                                        true)));

        assertThatThrownBy(() -> customers.save(invalid))
                .isInstanceOf(InvalidDataAccessApiUsageException.class)
                .hasMessageContaining("is not owned by customer")
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        var actual = inTransaction(() -> customers.findById(customerId).orElseThrow());
        assertThat(actual.address(originalDefaultId).defaultShipping()).isTrue();
        assertThat(actual.address(originalDefaultId).defaultBilling()).isTrue();
        assertThat(actual.addresses()).hasSize(1);
    }

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
        var thirdCreatedAt = Instant.parse("2026-09-17T12:00:00Z");
        jdbc.update(
                "update customer set created_at = ? where id = ?",
                Timestamp.from(thirdCreatedAt),
                third);

        var page = customers.searchForAdministration(new CustomerAdminSearch(0, 2, null));

        assertThat(page.content())
                .extracting(summary -> summary.customerId().value())
                .containsExactly(third, second);
        assertThat(page.content().getFirst().createdAt()).isEqualTo(thirdCreatedAt);
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
        var createdAt = Instant.parse("2026-09-17T12:00:00Z");
        jdbc.update(
                "update customer set created_at = ? where id = ?",
                Timestamp.from(createdAt),
                saved.id().orElseThrow().value());

        var detail = customers.findForAdministration(saved.id().orElseThrow()).orElseThrow();

        assertThat(detail.customerId()).isEqualTo(saved.id().orElseThrow());
        assertThat(detail.accountId()).isEqualTo(new AccountId(accountId));
        assertThat(detail.givenName()).isEqualTo("Ada");
        assertThat(detail.familyName()).isEqualTo("Lovelace");
        assertThat(detail.contactEmail()).isEqualTo("contact@example.com");
        assertThat(detail.createdAt()).isEqualTo(createdAt);
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

    @Test
    void includesCustomersCreatedAtTheStartAndExcludesCustomersCreatedAtTheEnd() {
        var atFrom = saveCustomer("Boundary", "From", "at-from@example.com");
        var inside = saveCustomer("Boundary", "Inside", "inside-range@example.com");
        var atBefore = saveCustomer("Boundary", "Before", "at-before@example.com");
        var from = Instant.parse("2026-08-18T12:00:00Z");
        var insideCreatedAt = Instant.parse("2026-09-01T12:00:00Z");
        var before = Instant.parse("2026-09-17T12:00:00Z");
        jdbc.update(
                "update customer set created_at = ? where id = ?", Timestamp.from(from), atFrom);
        jdbc.update(
                "update customer set created_at = ? where id = ?",
                Timestamp.from(insideCreatedAt),
                inside);
        jdbc.update(
                "update customer set created_at = ? where id = ?",
                Timestamp.from(before),
                atBefore);

        var page =
                customers.searchForAdministration(
                        new CustomerAdminSearch(0, 20, "Boundary", from, before));

        assertThat(page.content())
                .extracting(summary -> summary.customerId().value())
                .containsExactlyInAnyOrder(atFrom, inside)
                .doesNotContain(atBefore);
        assertThat(page.content())
                .extracting(summary -> summary.createdAt())
                .containsExactlyInAnyOrder(from, insideCreatedAt);
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

    private AddressDetails testCountryAddress() {
        return new AddressDetails(
                "Ada Lovelace",
                null,
                "2 Country Lane",
                null,
                "Elsewhere",
                null,
                "10115",
                "DE",
                null);
    }

    private AddressDetails withCity(AddressDetails address, String city) {
        return new AddressDetails(
                address.recipientName(),
                address.companyName(),
                address.addressLine1(),
                address.addressLine2(),
                city,
                address.region(),
                address.postalCode(),
                address.countryCode(),
                address.phoneNumber());
    }

    private <T> T inTransaction(Supplier<T> action) {
        return new TransactionTemplate(transactions).execute(status -> action.get());
    }
}
