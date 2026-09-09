package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;

import com.springbootecommerce.shophappens.catalog.application.port.in.ProductRevision;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.ordering.application.exception.CheckoutItemUnavailableException;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutReference;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlaceOrderCommand;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlaceOrderUseCase;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlacedOrder;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.util.AopTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

class CheckoutVariantConcurrencyIT extends AbstractIntegrationTest {
    @Autowired PlaceOrderUseCase checkout;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactions;
    @MockitoSpyBean ProductRepository products;

    @BeforeEach
    void resetFixture() {
        jdbc.execute(
                "truncate table integration_outbox, customer_order, customer_cart, product restart identity cascade");
    }

    @Test
    void interleavedVariantsDoNotDeadlock() throws Exception {
        var commands = interleavedCarts();
        var start = new CyclicBarrier(2);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<PlacedOrder> a =
                    pool.submit(
                            () -> {
                                start.await();
                                return checkout.place(commands.get(0));
                            });
            Future<PlacedOrder> b =
                    pool.submit(
                            () -> {
                                start.await();
                                return checkout.place(commands.get(1));
                            });
            assertThat(a.get(20, TimeUnit.SECONDS).order())
                    .isNotEqualTo(b.get(20, TimeUnit.SECONDS).order());
        } finally {
            pool.shutdownNow();
        }
        assertThat(
                        jdbc.queryForObject(
                                "select sum(stock_quantity) from product_variant", Long.class))
                .isEqualTo(36L);
        assertThat(jdbc.queryForObject("select count(*) from customer_order", Long.class))
                .isEqualTo(2L);
        assertThat(jdbc.queryForObject("select count(*) from integration_outbox", Long.class))
                .isEqualTo(2L);
        assertThat(jdbc.queryForObject("select count(*) from customer_cart_item", Long.class))
                .isZero();
    }

    @Test
    void concurrentSiblingPurchasesOnlyChangeSelectedStock() throws Exception {
        long product = CheckoutSeeds.seedProduct(jdbc, 10);
        long selectedSibling = sibling(product, "SELECTED");
        long untouchedSibling = sibling(product, "UNTOUCHED");
        long defaultVariant = defaultVariant(product);
        var first = CheckoutSeeds.seedCustomerCart(jdbc, product, 1);
        var second = CheckoutSeeds.seedCustomerCart(jdbc, product, 1);
        jdbc.update(
                "update customer_cart_item set variant_id = ? where cart_id = ?",
                selectedSibling,
                second.cartId());

        List<PlacedOrder> orders = placeTogether(command(first), command(second));

        assertThat(orders.get(0).order()).isNotEqualTo(orders.get(1).order());
        assertThat(stock(defaultVariant)).isEqualTo(9);
        assertThat(stock(selectedSibling)).isEqualTo(9);
        assertThat(stock(untouchedSibling)).isEqualTo(10);
        assertThat(jdbc.queryForObject("select count(*) from customer_order", Long.class))
                .isEqualTo(2L);
    }

    @Test
    void checkoutWaitsForAdministrationAndSeesCommittedStock() throws Exception {
        long product = CheckoutSeeds.seedProduct(jdbc, 10);
        long variant = defaultVariant(product);
        String sku =
                jdbc.queryForObject(
                        "select sku from product_variant where id = ?", String.class, variant);
        var customer = CheckoutSeeds.seedCustomerCart(jdbc, product, 1);
        var adminLocked = new CountDownLatch(1);
        var releaseAdmin = new CountDownLatch(1);
        var purchaseStarted = new CountDownLatch(1);
        ProductRepository target = AopTestUtils.getUltimateTargetObject(products);
        doAnswer(
                        call -> {
                            purchaseStarted.countDown();
                            return call.callRealMethod();
                        })
                .when(target)
                .findAllForPurchase(anyList());

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<?> writer =
                    pool.submit(
                            () ->
                                    new TransactionTemplate(transactions)
                                            .executeWithoutResult(
                                                    status -> {
                                                        var loaded =
                                                                products.findForAdministrationUpdate(
                                                                                new ProductId(
                                                                                        product))
                                                                        .orElseThrow();
                                                        adminLocked.countDown();
                                                        try {
                                                            if (!releaseAdmin.await(
                                                                    10, TimeUnit.SECONDS)) {
                                                                throw new IllegalStateException(
                                                                        "Timed out waiting to release admin");
                                                            }
                                                        } catch (InterruptedException exception) {
                                                            Thread.currentThread().interrupt();
                                                            throw new IllegalStateException(
                                                                    exception);
                                                        }
                                                        loaded.product()
                                                                .reviseVariant(
                                                                        new ProductVariantId(
                                                                                variant),
                                                                        new Sku(sku),
                                                                        loaded.product()
                                                                                .variant(
                                                                                        new ProductVariantId(
                                                                                                variant))
                                                                                .price(),
                                                                        0,
                                                                        null,
                                                                        true);
                                                        products.updateForAdministration(
                                                                loaded.product(),
                                                                new ProductRevision(
                                                                        loaded.revision()));
                                                    }));
            assertThat(adminLocked.await(10, TimeUnit.SECONDS)).isTrue();
            Future<PlacedOrder> placement = pool.submit(() -> checkout.place(command(customer)));
            assertThat(purchaseStarted.await(10, TimeUnit.SECONDS)).isTrue();
            assertThat(placement.isDone()).isFalse();

            releaseAdmin.countDown();
            writer.get(20, TimeUnit.SECONDS);
            assertThatThrownBy(() -> placement.get(20, TimeUnit.SECONDS))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(CheckoutItemUnavailableException.class);
        } finally {
            releaseAdmin.countDown();
            pool.shutdownNow();
        }

        assertThat(stock(variant)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from customer_order", Long.class)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from integration_outbox", Long.class))
                .isZero();
        assertThat(
                        jdbc.queryForObject(
                                "select count(*) from customer_cart_item where cart_id = ?",
                                Long.class,
                                customer.cartId()))
                .isOne();
    }

    private List<PlaceOrderCommand> interleavedCarts() {
        long p1 = CheckoutSeeds.seedProduct(jdbc, 10);
        long p2 = CheckoutSeeds.seedProduct(jdbc, 10);
        long v3 = sibling(p2, "P2-SIBLING");
        long v4 = sibling(p1, "P1-SIBLING");
        var a = CheckoutSeeds.seedCustomerCart(jdbc, p1, 1);
        var b = CheckoutSeeds.seedCustomerCart(jdbc, p2, 1);
        jdbc.update(
                "insert into customer_cart_item(cart_id,variant_id,quantity) values (?,?,1)",
                a.cartId(),
                v3);
        jdbc.update(
                "insert into customer_cart_item(cart_id,variant_id,quantity) values (?,?,1)",
                b.cartId(),
                v4);
        return List.of(command(a), command(b));
    }

    private long sibling(long product, String prefix) {
        return jdbc.queryForObject(
                """
                insert into product_variant(product_id,sku,price,stock_quantity,active,is_default)
                values (?, ?, 20.00, 10, true, false) returning id
                """,
                Long.class,
                product,
                prefix + UUID.randomUUID().toString().substring(0, 8));
    }

    private long defaultVariant(long product) {
        return jdbc.queryForObject(
                "select id from product_variant where product_id = ? and is_default = true",
                Long.class,
                product);
    }

    private int stock(long variant) {
        return jdbc.queryForObject(
                "select stock_quantity from product_variant where id = ?", Integer.class, variant);
    }

    private List<PlacedOrder> placeTogether(PlaceOrderCommand first, PlaceOrderCommand second)
            throws Exception {
        var start = new CyclicBarrier(2);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<PlacedOrder> a =
                    pool.submit(
                            () -> {
                                start.await();
                                return checkout.place(first);
                            });
            Future<PlacedOrder> b =
                    pool.submit(
                            () -> {
                                start.await();
                                return checkout.place(second);
                            });
            return List.of(a.get(20, TimeUnit.SECONDS), b.get(20, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
        }
    }

    private PlaceOrderCommand command(CheckoutSeeds.CustomerCartSeed customer) {
        return new PlaceOrderCommand(
                new CustomerId(customer.customerId()),
                new CheckoutReference(UUID.randomUUID()),
                customer.shippingAddressId(),
                customer.billingAddressId(),
                null);
    }
}
