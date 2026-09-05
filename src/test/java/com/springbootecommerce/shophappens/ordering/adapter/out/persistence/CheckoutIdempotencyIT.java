package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.ordering.adapter.out.persistence.CheckoutSeeds.Seed;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutReference;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlaceOrderCommand;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlaceOrderUseCase;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlacedOrder;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class CheckoutIdempotencyIT extends AbstractIntegrationTest {
    @Autowired PlaceOrderUseCase checkout;
    @Autowired JdbcTemplate jdbc;

    @Test
    void concurrentCheckoutsWithTheSameCustomerAndCheckoutCreateOneOrderAndOneStockDeduction()
            throws Exception {
        Seed seed = CheckoutSeeds.seed(jdbc);
        CheckoutReference checkoutId = new CheckoutReference(UUID.randomUUID());
        PlaceOrderCommand command =
                new PlaceOrderCommand(
                        new CustomerId(seed.customerId()),
                        checkoutId,
                        seed.shippingAddressId(),
                        seed.billingAddressId());

        CyclicBarrier barrier = new CyclicBarrier(2);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<PlacedOrder> first =
                    pool.submit(() -> placeAfterBarrier(barrier, checkout, command));
            Future<PlacedOrder> second =
                    pool.submit(() -> placeAfterBarrier(barrier, checkout, command));

            PlacedOrder firstResult = first.get(45, SECONDS);
            PlacedOrder secondResult = second.get(45, SECONDS);

            assertThat(firstResult.order()).isEqualTo(secondResult.order());
            assertThat(firstResult.orderNumber()).isEqualTo(secondResult.orderNumber());
            assertThat(firstResult.total()).isEqualTo(secondResult.total());
        } finally {
            pool.shutdownNow();
        }

        assertThat(
                        jdbc.queryForObject(
                                """
                                select count(*) from customer_order
                                where customer_id = ? and checkout_id = ?
                                """,
                                Long.class,
                                seed.customerId(),
                                checkoutId.value()))
                .isOne();
        assertThat(
                        jdbc.queryForObject(
                                "select stock_quantity from product where id = ?",
                                Integer.class,
                                seed.productId()))
                .isEqualTo(CheckoutSeeds.INITIAL_STOCK - CheckoutSeeds.QUANTITY);
        assertThat(jdbc.queryForObject("select count(*) from integration_outbox", Long.class))
                .isOne();
        assertThat(jdbc.queryForObject("select published_at from integration_outbox", Object.class))
                .isNull();
        assertThat(
                        jdbc.queryForObject(
                                "select count(*) from customer_cart_item where cart_id = ?",
                                Long.class,
                                seed.cartId()))
                .isZero();
    }

    private static PlacedOrder placeAfterBarrier(
            CyclicBarrier barrier, PlaceOrderUseCase checkout, PlaceOrderCommand command) {
        try {
            barrier.await();
        } catch (Exception e) {
            throw new IllegalStateException("barrier interrupted", e);
        }
        return checkout.place(command);
    }
}
