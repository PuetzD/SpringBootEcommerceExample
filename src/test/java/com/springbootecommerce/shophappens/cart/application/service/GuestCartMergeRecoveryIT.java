package com.springbootecommerce.shophappens.cart.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.springbootecommerce.shophappens.account.application.port.in.AccountReference;
import com.springbootecommerce.shophappens.account.application.port.in.AuthenticatedAccountIdentity;
import com.springbootecommerce.shophappens.cart.application.port.in.CustomerCartQuery;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartConsumedException;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartReference;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartUseCase;
import com.springbootecommerce.shophappens.cart.application.port.in.MergeGuestCartUseCase;
import com.springbootecommerce.shophappens.cart.application.port.out.CartMergeLedger;
import com.springbootecommerce.shophappens.cart.application.port.out.CustomerCartRepository;
import com.springbootecommerce.shophappens.cart.application.port.out.GuestCartRepository;
import com.springbootecommerce.shophappens.cart.domain.model.Cart;
import com.springbootecommerce.shophappens.cart.domain.model.CartId;
import com.springbootecommerce.shophappens.cart.domain.model.CartOwner;
import com.springbootecommerce.shophappens.cart.domain.model.GuestCartId;
import com.springbootecommerce.shophappens.cart.domain.model.Quantity;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReferenceQuery;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.security.service.CartMergingAuthenticationSuccessHandler;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@Import(GuestCartMergeRecoveryIT.GuestCartStoreConfiguration.class)
class GuestCartMergeRecoveryIT extends AbstractIntegrationTest {
    private static final CustomerReference CUSTOMER = new CustomerReference(9_000_001L);
    private static final ProductVariantId VARIANT = new ProductVariantId(9_000_001L);

    @Autowired MergeGuestCartUseCase mergeGuestCart;
    @Autowired CustomerCartQuery customerCarts;
    @Autowired CartMergeLedger ledger;
    @Autowired GuestCartUseCase guestCartUseCase;
    @Autowired ControllableGuestCartStore guests;
    @Autowired JdbcTemplate jdbc;
    @MockitoSpyBean CustomerCartRepository customers;

    @BeforeEach
    void resetFixture() {
        guests.reset();
        jdbc.update("delete from consumed_guest_cart where customer_id=?", CUSTOMER.value());
        jdbc.update("delete from customer_cart where customer_id=?", CUSTOMER.value());
    }

    @Test
    void retriesCommittedMergeAfterGuestCartCleanupFailureWithoutDuplicatingQuantity()
            throws Exception {
        GuestCartId guestId = GuestCartId.random();
        guests.save(guestCart(guestId, 3));
        CartMergingAuthenticationSuccessHandler handler =
                new CartMergingAuthenticationSuccessHandler(mergeGuestCart, customerLookup());
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(
                        (AuthenticatedAccountIdentity) () -> new AccountReference(9_000_001L),
                        null);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(GuestCartReference.SESSION_ATTRIBUTE, guestId.value().toString());

        authenticate(handler, session, authentication);

        assertThat(session.getAttribute(GuestCartReference.SESSION_ATTRIBUTE))
                .isEqualTo(guestId.value().toString());
        assertThat(guests.find(guestId)).isPresent();
        assertThat(ledger.claim(guestId, new CustomerId(CUSTOMER.value()))).isFalse();
        assertThat(customerCarts.get(new CustomerId(CUSTOMER.value())).items())
                .singleElement()
                .satisfies(
                        item -> {
                            assertThat(item.variant()).isEqualTo(VARIANT);
                            assertThat(item.quantity()).isEqualTo(3);
                        });
        assertThatThrownBy(
                        () ->
                                guestCartUseCase.add(
                                        new GuestCartReference(guestId.value()), VARIANT, 1))
                .isInstanceOf(GuestCartConsumedException.class);

        authenticate(handler, session, authentication);

        assertThat(session.getAttribute(GuestCartReference.SESSION_ATTRIBUTE)).isNull();
        assertThat(guests.find(guestId)).isEmpty();
        assertThat(customerCarts.get(new CustomerId(CUSTOMER.value())).items())
                .singleElement()
                .satisfies(item -> assertThat(item.quantity()).isEqualTo(3));
    }

    @Test
    void sqlFailureRollsBackConsumptionAndLeavesGuestWritable() {
        GuestCartId guestId = GuestCartId.random();
        guests.save(guestCart(guestId, 3));
        doThrow(new IllegalStateException("customer cart unavailable"))
                .when(customers)
                .save(any(Cart.class));

        assertThatThrownBy(
                        () ->
                                mergeGuestCart.merge(
                                        new GuestCartReference(guestId.value()),
                                        new CustomerId(CUSTOMER.value())))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("customer cart unavailable");

        assertThat(ledger.isConsumed(guestId)).isFalse();
        assertThat(guests.find(guestId)).isPresent();
        guestCartUseCase.add(new GuestCartReference(guestId.value()), VARIANT, 1);
        assertThat(guestCartUseCase.getSnapshot(new GuestCartReference(guestId.value())).items())
                .singleElement()
                .satisfies(item -> assertThat(item.quantity()).isEqualTo(4));
    }

    @Test
    void redisReadFailureRollsBackClaimAndPreservesSessionIdentifier() throws Exception {
        GuestCartId guestId = GuestCartId.random();
        guests.save(guestCart(guestId, 3));
        guests.failNextFind();
        CartMergingAuthenticationSuccessHandler handler =
                new CartMergingAuthenticationSuccessHandler(mergeGuestCart, customerLookup());
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(
                        (AuthenticatedAccountIdentity) () -> new AccountReference(9_000_001L),
                        null);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(GuestCartReference.SESSION_ATTRIBUTE, guestId.value().toString());

        authenticate(handler, session, authentication);

        assertThat(session.getAttribute(GuestCartReference.SESSION_ATTRIBUTE))
                .isEqualTo(guestId.value().toString());
        assertThat(ledger.isConsumed(guestId)).isFalse();
        assertThat(guests.find(guestId)).isPresent();
    }

    private static CustomerReferenceQuery customerLookup() {
        return account -> Optional.of(CUSTOMER);
    }

    private static void authenticate(
            CartMergingAuthenticationSuccessHandler handler,
            MockHttpSession session,
            TestingAuthenticationToken authentication)
            throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        handler.onAuthenticationSuccess(request, new MockHttpServletResponse(), authentication);
    }

    private static Cart guestCart(GuestCartId guestId, int quantity) {
        Cart cart = Cart.empty(CartId.random(), new CartOwner.Guest(guestId));
        cart.changeQuantity(VARIANT, new Quantity(quantity));
        return cart;
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class GuestCartStoreConfiguration {
        @Bean
        @Primary
        ControllableGuestCartStore controllableGuestCartStore() {
            return new ControllableGuestCartStore();
        }
    }

    static final class ControllableGuestCartStore implements GuestCartRepository {
        private final Map<GuestCartId, Cart> carts = new ConcurrentHashMap<>();
        private boolean failNextDelete = true;
        private boolean failNextFind;

        @Override
        public Optional<Cart> find(GuestCartId id) {
            if (failNextFind) {
                failNextFind = false;
                throw new IllegalStateException("Redis unavailable");
            }
            return Optional.ofNullable(carts.get(id));
        }

        @Override
        public Cart save(Cart cart) {
            GuestCartId id = ((CartOwner.Guest) cart.owner()).id();
            carts.put(id, cart);
            return cart;
        }

        @Override
        public void delete(GuestCartId id) {
            if (failNextDelete) {
                failNextDelete = false;
                throw new IllegalStateException("Redis unavailable");
            }
            carts.remove(id);
        }

        void failNextFind() {
            failNextFind = true;
        }

        void reset() {
            carts.clear();
            failNextDelete = true;
            failNextFind = false;
        }
    }
}
