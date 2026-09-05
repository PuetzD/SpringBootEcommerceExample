package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.ordering.adapter.out.persistence.CheckoutSeeds.CustomerCartSeed;
import com.springbootecommerce.shophappens.ordering.application.exception.CheckoutItemUnavailableException;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutReference;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlaceOrderCommand;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlaceOrderUseCase;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlacedOrder;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class CheckoutConcurrencyIT extends AbstractIntegrationTest {
    @Autowired PlaceOrderUseCase checkout;
    @Autowired JdbcTemplate jdbc;

    @Test
    void twoDistinctCustomersCannotOversellOneUnit() throws Exception {
        long productId = CheckoutSeeds.seedProduct(jdbc, 1);
        CustomerCartSeed firstCustomer = CheckoutSeeds.seedCustomerCart(jdbc, productId, 1);
        CustomerCartSeed secondCustomer = CheckoutSeeds.seedCustomerCart(jdbc, productId, 1);
        PlaceOrderCommand firstCommand = command(firstCustomer);
        PlaceOrderCommand secondCommand = command(secondCustomer);

        CyclicBarrier barrier = new CyclicBarrier(2);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        List<Attempt> attempts;
        try {
            Future<Attempt> first =
                    pool.submit(
                            () ->
                                    placeAfterBarrier(
                                            barrier, checkout, firstCustomer, firstCommand));
            Future<Attempt> second =
                    pool.submit(
                            () ->
                                    placeAfterBarrier(
                                            barrier, checkout, secondCustomer, secondCommand));
            attempts = List.of(first.get(45, SECONDS), second.get(45, SECONDS));
        } finally {
            pool.shutdownNow();
        }

        List<Attempt> successes = attempts.stream().filter(Attempt::succeeded).toList();
        List<Attempt> failures = attempts.stream().filter(attempt -> !attempt.succeeded()).toList();
        assertThat(successes)
                .as(
                        "attempts: %s, causes: %s",
                        attempts,
                        failures.stream().map(attempt -> attempt.failure().getCause()).toList())
                .hasSize(1);
        assertThat(failures).as("attempts: %s", attempts).hasSize(1);
        assertThat(failures.getFirst().failure())
                .isInstanceOf(CheckoutItemUnavailableException.class);

        assertThat(jdbc.queryForObject("select count(*) from customer_order", Long.class)).isOne();
        assertThat(jdbc.queryForObject("select count(*) from integration_outbox", Long.class))
                .isOne();
        assertThat(
                        jdbc.queryForObject(
                                "select stock_quantity from product where id = ?",
                                Integer.class,
                                productId))
                .isZero();
        assertThat(cartItemCount(successes.getFirst().customer().cartId())).isZero();
        assertThat(cartItemCount(failures.getFirst().customer().cartId())).isOne();
    }

    private long cartItemCount(UUID cartId) {
        return jdbc.queryForObject(
                "select count(*) from customer_cart_item where cart_id = ?", Long.class, cartId);
    }

    private static PlaceOrderCommand command(CustomerCartSeed customer) {
        return new PlaceOrderCommand(
                new CustomerId(customer.customerId()),
                new CheckoutReference(UUID.randomUUID()),
                customer.shippingAddressId(),
                customer.billingAddressId());
    }

    private static Attempt placeAfterBarrier(
            CyclicBarrier barrier,
            PlaceOrderUseCase checkout,
            CustomerCartSeed customer,
            PlaceOrderCommand command) {
        try {
            barrier.await();
            return Attempt.success(customer, checkout.place(command));
        } catch (CheckoutItemUnavailableException exception) {
            return Attempt.failure(customer, exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Concurrent checkout failed unexpectedly", exception);
        }
    }

    private record Attempt(
            CustomerCartSeed customer, PlacedOrder placedOrder, RuntimeException failure) {
        static Attempt success(CustomerCartSeed customer, PlacedOrder placedOrder) {
            return new Attempt(customer, placedOrder, null);
        }

        static Attempt failure(CustomerCartSeed customer, RuntimeException failure) {
            return new Attempt(customer, null, failure);
        }

        boolean succeeded() {
            return placedOrder != null;
        }
    }
}
