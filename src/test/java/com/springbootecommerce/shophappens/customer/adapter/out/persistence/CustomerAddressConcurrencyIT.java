package com.springbootecommerce.shophappens.customer.adapter.out.persistence;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.shophappens.customer.application.port.out.CustomerRepository;
import com.springbootecommerce.shophappens.customer.domain.model.AddressDetails;
import com.springbootecommerce.shophappens.customer.domain.model.ContactEmail;
import com.springbootecommerce.shophappens.customer.domain.model.Customer;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

class CustomerAddressConcurrencyIT extends AbstractIntegrationTest {
    @Autowired CustomerRepository customers;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactions;

    @Test
    void serializesMutationsAndPreservesBothCommittedChanges() throws Exception {
        var saved = createCustomer("serialized-addresses@example.com");
        CustomerId customerId = saved.id().orElseThrow();
        var addressId = saved.addresses().getFirst().id().orElseThrow();
        var firstAcquired = new CountDownLatch(1);
        var secondStarted = new CountDownLatch(1);
        var secondAcquired = new CountDownLatch(1);
        var releaseFirst = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Void> first =
                    pool.submit(
                            () ->
                                    inTransaction(
                                            () -> {
                                                var customer =
                                                        customers
                                                                .findForUpdate(customerId)
                                                                .orElseThrow();
                                                firstAcquired.countDown();
                                                await(releaseFirst);
                                                customer.updateAddress(
                                                        addressId,
                                                        withCity(
                                                                customer.address(addressId)
                                                                        .details(),
                                                                "Berlin"),
                                                        true,
                                                        true);
                                                customers.save(customer);
                                                return null;
                                            }));
            assertThat(firstAcquired.await(10, SECONDS)).isTrue();

            Future<String> second =
                    pool.submit(
                            () ->
                                    inTransaction(
                                            () -> {
                                                secondStarted.countDown();
                                                var customer =
                                                        customers
                                                                .findForUpdate(customerId)
                                                                .orElseThrow();
                                                secondAcquired.countDown();
                                                String observedCity =
                                                        customer.address(addressId)
                                                                .details()
                                                                .city();
                                                customer.addAddress(
                                                        testCountryAddress(), false, false);
                                                customers.save(customer);
                                                return observedCity;
                                            }));
            assertThat(secondStarted.await(10, SECONDS)).isTrue();
            assertThat(secondAcquired.await(250, MILLISECONDS)).isFalse();

            releaseFirst.countDown();
            first.get(10, SECONDS);
            assertThat(second.get(10, SECONDS)).isEqualTo("Berlin");
        } finally {
            releaseFirst.countDown();
            pool.shutdownNow();
        }

        var actual = inTransaction(() -> customers.findById(customerId).orElseThrow());
        assertThat(actual.address(addressId).details().city()).isEqualTo("Berlin");
        assertThat(actual.addresses()).hasSize(2);
        assertThat(actual.addresses()).filteredOn(address -> address.defaultShipping()).hasSize(1);
        assertThat(actual.addresses()).filteredOn(address -> address.defaultBilling()).hasSize(1);
    }

    @Test
    void doesNotBlockAMutationForADifferentCustomer() throws Exception {
        var firstCustomer = createCustomer("first-lock-scope@example.com");
        var secondCustomer = createCustomer("second-lock-scope@example.com");
        var secondAddressId = secondCustomer.addresses().getFirst().id().orElseThrow();
        var firstAcquired = new CountDownLatch(1);
        var releaseFirst = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Void> first =
                    pool.submit(
                            () ->
                                    inTransaction(
                                            () -> {
                                                customers
                                                        .findForUpdate(
                                                                firstCustomer.id().orElseThrow())
                                                        .orElseThrow();
                                                firstAcquired.countDown();
                                                await(releaseFirst);
                                                return null;
                                            }));
            assertThat(firstAcquired.await(10, SECONDS)).isTrue();

            Future<CustomerId> second =
                    pool.submit(
                            () ->
                                    inTransaction(
                                            () -> {
                                                var customer =
                                                        customers
                                                                .findForUpdate(
                                                                        secondCustomer
                                                                                .id()
                                                                                .orElseThrow())
                                                                .orElseThrow();
                                                customer.updateAddress(
                                                        secondAddressId,
                                                        withCity(
                                                                customer.address(secondAddressId)
                                                                        .details(),
                                                                "Hamburg"),
                                                        true,
                                                        true);
                                                customers.save(customer);
                                                return customer.id().orElseThrow();
                                            }));

            assertThat(second.get(5, SECONDS)).isEqualTo(secondCustomer.id().orElseThrow());
            releaseFirst.countDown();
            first.get(10, SECONDS);
        } finally {
            releaseFirst.countDown();
            pool.shutdownNow();
        }

        var actual =
                inTransaction(
                        () -> customers.findById(secondCustomer.id().orElseThrow()).orElseThrow());
        assertThat(actual.address(secondAddressId).details().city()).isEqualTo("Hamburg");
    }

    private Customer createCustomer(String email) {
        long account =
                jdbc.queryForObject(
                        "insert into account (email, password_hash, role) values (?, ?, ?) returning id",
                        Long.class,
                        email,
                        "encoded",
                        "CUSTOMER");
        return inTransaction(
                () -> {
                    var customer =
                            Customer.create(
                                    new AccountId(account),
                                    "Ada",
                                    "Lovelace",
                                    new ContactEmail(email));
                    customer.addAddress(testCityAddress(), true, true);
                    return customers.save(customer);
                });
    }

    private <T> T inTransaction(Supplier<T> action) {
        return new TransactionTemplate(transactions).execute(status -> action.get());
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, SECONDS)) {
                throw new IllegalStateException("Timed out waiting for concurrent test step");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Interrupted while waiting for concurrent test step", exception);
        }
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
}
