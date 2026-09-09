package com.springbootecommerce.shophappens.cart.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;

import com.springbootecommerce.shophappens.cart.application.port.in.CartItemSnapshot;
import com.springbootecommerce.shophappens.cart.application.port.in.CustomerCartQuery;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartConsumedException;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartReference;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartUseCase;
import com.springbootecommerce.shophappens.cart.application.port.in.MergeGuestCartUseCase;
import com.springbootecommerce.shophappens.cart.application.port.out.CartMergeLedger;
import com.springbootecommerce.shophappens.cart.application.port.out.GuestCartRepository;
import com.springbootecommerce.shophappens.cart.domain.model.GuestCartId;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@TestPropertySource(properties = "cart.guest.lock-timeout=10s")
class GuestCartConcurrencyIT extends AbstractIntegrationTest {
    @Autowired GuestCartUseCase guestCart;
    @Autowired MergeGuestCartUseCase mergeGuestCart;
    @Autowired CustomerCartQuery customerCarts;
    @Autowired CartMergeLedger ledger;
    @Autowired ProductRepository catalogProducts;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactions;
    @MockitoSpyBean GuestCartRepository guests;

    @Test
    void concurrentAddsRetainBothSuccessfulSelections() throws Exception {
        var guest = new GuestCartReference(UUID.randomUUID());
        var guestId = new GuestCartId(guest.value());
        var firstRead = new CountDownLatch(1);
        var secondRead = new CountDownLatch(1);
        var reads = new AtomicInteger();
        doAnswer(
                        call -> {
                            Object result = call.callRealMethod();
                            if (reads.incrementAndGet() == 1) {
                                firstRead.countDown();
                                secondRead.await(5, TimeUnit.SECONDS);
                            } else {
                                secondRead.countDown();
                            }
                            return result;
                        })
                .when(guests)
                .find(guestId);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<?> first = pool.submit(() -> guestCart.add(guest, new ProductVariantId(101), 1));
            assertThat(firstRead.await(5, TimeUnit.SECONDS)).isTrue();
            Future<?> second =
                    pool.submit(() -> guestCart.add(guest, new ProductVariantId(202), 1));

            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }

        assertThat(guestCart.getSnapshot(guest).items())
                .containsExactly(
                        new CartItemSnapshot(new ProductVariantId(101), 1),
                        new CartItemSnapshot(new ProductVariantId(202), 1));
    }

    @Test
    void consumedGuestRejectsNewIntentWithoutChangingRedis() {
        var guest = new GuestCartReference(UUID.randomUUID());
        var id = new GuestCartId(guest.value());
        CustomerId customer = seedCustomer();
        guestCart.add(guest, new ProductVariantId(101), 3);
        new TransactionTemplate(transactions)
                .executeWithoutResult(status -> ledger.claim(id, customer));

        assertThatThrownBy(() -> guestCart.add(guest, new ProductVariantId(202), 1))
                .isInstanceOf(GuestCartConsumedException.class);
        assertThat(guestCart.getSnapshot(guest).items())
                .containsExactly(new CartItemSnapshot(new ProductVariantId(101), 3));
    }

    @Test
    void mutationArrivingDuringMergeIsExplicitlyRejected() throws Exception {
        CustomerId customer = seedCustomer();
        Product saved =
                catalogProducts.save(
                        Product.create(
                                new Sku("MERGE-" + UUID.randomUUID().toString().substring(0, 8)),
                                "Merge fixture",
                                "Fixture",
                                new Money(new BigDecimal("10.00")),
                                10,
                                null,
                                Set.of()));
        ProductVariantId variant = saved.defaultVariant().id().orElseThrow();
        var guest = new GuestCartReference(UUID.randomUUID());
        var id = new GuestCartId(guest.value());
        guestCart.add(guest, variant, 3);
        var read = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var pause = new AtomicBoolean(true);
        doAnswer(
                        call -> {
                            Object result = call.callRealMethod();
                            if (pause.getAndSet(false)) {
                                read.countDown();
                                release.await(5, TimeUnit.SECONDS);
                            }
                            return result;
                        })
                .when(guests)
                .find(id);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<?> merge = pool.submit(() -> mergeGuestCart.merge(guest, customer));
            assertThat(read.await(5, TimeUnit.SECONDS)).isTrue();
            Future<?> late = pool.submit(() -> guestCart.add(guest, variant, 1));
            release.countDown();
            merge.get(10, TimeUnit.SECONDS);
            assertThatThrownBy(() -> late.get(10, TimeUnit.SECONDS))
                    .hasCauseInstanceOf(GuestCartConsumedException.class);
        } finally {
            release.countDown();
            pool.shutdownNow();
        }

        assertThat(customerCarts.get(customer).items())
                .containsExactly(new CartItemSnapshot(variant, 3));
        mergeGuestCart.merge(guest, customer);
        assertThat(customerCarts.get(customer).items())
                .containsExactly(new CartItemSnapshot(variant, 3));
    }

    private CustomerId seedCustomer() {
        String email = "cart-" + UUID.randomUUID() + "@example.com";
        Long account =
                jdbc.queryForObject(
                        """
                        insert into account(email,password_hash,role)
                        values (?,'encoded','CUSTOMER') returning id
                        """,
                        Long.class,
                        email);
        Long customer =
                jdbc.queryForObject(
                        """
                        insert into customer(account_id,given_name,family_name,contact_email)
                        values (?,'Guest','Merge',?) returning id
                        """,
                        Long.class,
                        account,
                        email);
        return new CustomerId(customer);
    }
}
