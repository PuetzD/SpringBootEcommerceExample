package com.springbootecommerce.shophappens.cart.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.springbootecommerce.shophappens.cart.domain.Cart;
import com.springbootecommerce.shophappens.cart.domain.Quantity;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

class CartRepositoryIT extends AbstractIntegrationTest {

    @Autowired CartRepository repository;
    @Autowired EntityManager entityManager;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired TransactionTemplate transactionTemplate;

    @BeforeEach
    void clearCarts() {
        jdbcTemplate.update("DELETE FROM cart_item");
        jdbcTemplate.update("DELETE FROM cart");
    }

    @Test
    void persistsAggregateAndOwnedItems() {
        var cart = Cart.forCustomer(42L);
        cart.addProduct(7L, new Quantity(2));
        repository.saveAndFlush(cart);
        entityManager.clear();

        var reloaded = repository.findByCustomerId(42L).orElseThrow();
        assertThat(reloaded.items()).hasSize(1);
        assertThat(reloaded.items().getFirst().productId()).isEqualTo(7L);
    }

    @Test
    void enforcesOneCartPerCustomerAndRemovesOwnedOrphans() {
        repository.saveAndFlush(Cart.forCustomer(42L));
        assertThatThrownBy(() -> repository.saveAndFlush(Cart.forCustomer(42L)))
                .isInstanceOf(DataIntegrityViolationException.class);

        var cart = Cart.forCustomer(43L);
        cart.addProduct(7L, new Quantity(1));
        repository.saveAndFlush(cart);
        cart.changeQuantity(7L, 0);
        repository.saveAndFlush(cart);
        assertThat(
                        jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM cart_item WHERE cart_id = ?",
                                Long.class,
                                cart.getId()))
                .isZero();
    }

    @Test
    void managesAggregateVersionColumn() {
        var cart = repository.saveAndFlush(Cart.forCustomer(44L));
        assertThat(
                        jdbcTemplate.queryForObject(
                                "SELECT version FROM cart WHERE id = ?", Long.class, cart.getId()))
                .isEqualTo(0L);
    }

    @Test
    void createsOneCartWhenTwoTransactionsEnsureItConcurrently() throws Exception {
        var executor = Executors.newFixedThreadPool(2);
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        List<java.util.concurrent.Future<Boolean>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < 2; i++) {
                futures.add(
                        executor.submit(
                                () -> {
                                    ready.countDown();
                                    try {
                                        start.await();
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }
                                    return transactionTemplate.execute(
                                            status -> {
                                                repository.ensureExistsForCustomer(45L);
                                                return true;
                                            });
                                }));
            }
            ready.await();
            start.countDown();
            for (var future : futures) {
                assertThat(future.get()).isTrue();
            }
            var count =
                    jdbcTemplate.queryForObject(
                            "SELECT COUNT(*) FROM cart WHERE customer_id = ?", Long.class, 45L);
            assertThat(count).isEqualTo(1L);
        } finally {
            executor.shutdownNow();
        }
    }
}
