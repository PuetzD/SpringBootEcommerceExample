package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutReference;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutReview;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutReviewChangedException;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlaceOrderCommand;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlaceOrderUseCase;
import com.springbootecommerce.shophappens.ordering.application.port.in.PrepareCheckoutUseCase;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class CheckoutReviewIT extends AbstractIntegrationTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired PrepareCheckoutUseCase preparation;
    @Autowired PlaceOrderUseCase checkout;
    @Autowired Clock clock;

    @BeforeEach
    void resetFixture() {
        jdbc.execute(
                "truncate table integration_outbox, customer_order, customer_cart, product restart identity cascade");
    }

    @Test
    void changedPriceRollsBackThenFreshReviewPlacesOnce() {
        long product = CheckoutSeeds.seedProduct(jdbc, 10);
        var seeded = CheckoutSeeds.seedCustomerCart(jdbc, product, 1);
        CustomerId customer = new CustomerId(seeded.customerId());
        CheckoutReference key = new CheckoutReference(UUID.randomUUID());
        var initial = preparation.prepare(customer);
        var line = initial.items().getFirst();
        var review =
                new CheckoutReview(customer, initial.items(), clock.instant().plusSeconds(900));
        var command =
                new PlaceOrderCommand(
                        customer,
                        key,
                        seeded.shippingAddressId(),
                        seeded.billingAddressId(),
                        review);
        var changedPrice = line.unitPrice().add(new Money(new BigDecimal("1.00")));
        jdbc.update(
                "update product_variant set price=? where id=?",
                changedPrice.amount(),
                line.variant().value());
        Long ordersBefore = jdbc.queryForObject("select count(*) from customer_order", Long.class);
        Long eventsBefore =
                jdbc.queryForObject("select count(*) from integration_outbox", Long.class);

        assertThatThrownBy(() -> checkout.place(command))
                .isInstanceOf(CheckoutReviewChangedException.class);

        assertThat(
                        jdbc.queryForObject(
                                "select stock_quantity from product_variant where id=?",
                                Integer.class,
                                line.variant().value()))
                .isEqualTo(10);
        assertThat(
                        jdbc.queryForObject(
                                "select quantity from customer_cart_item where cart_id=? and variant_id=?",
                                Integer.class,
                                seeded.cartId(),
                                line.variant().value()))
                .isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from customer_order", Long.class))
                .isEqualTo(ordersBefore);
        assertThat(jdbc.queryForObject("select count(*) from integration_outbox", Long.class))
                .isEqualTo(eventsBefore);

        var fresh =
                new CheckoutReview(
                        customer,
                        preparation.prepare(customer).items(),
                        clock.instant().plusSeconds(900));
        var placed =
                checkout.place(
                        new PlaceOrderCommand(
                                customer,
                                key,
                                seeded.shippingAddressId(),
                                seeded.billingAddressId(),
                                fresh));
        var replay =
                checkout.place(
                        new PlaceOrderCommand(
                                customer,
                                key,
                                seeded.shippingAddressId(),
                                seeded.billingAddressId(),
                                null));

        assertThat(replay.order()).isEqualTo(placed.order());
        assertThat(
                        jdbc.queryForObject(
                                "select stock_quantity from product_variant where id=?",
                                Integer.class,
                                line.variant().value()))
                .isEqualTo(9);
        assertThat(jdbc.queryForObject("select count(*) from customer_order", Long.class))
                .isEqualTo(ordersBefore + 1);
        assertThat(jdbc.queryForObject("select count(*) from integration_outbox", Long.class))
                .isEqualTo(eventsBefore + 1);
    }
}
