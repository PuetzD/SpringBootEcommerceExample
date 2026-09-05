package com.springbootecommerce.shophappens.cart.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.shophappens.account.application.port.in.AccountReference;
import com.springbootecommerce.shophappens.account.application.port.in.AuthenticatedAccountIdentity;
import com.springbootecommerce.shophappens.cart.application.port.in.CustomerCartQuery;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartReference;
import com.springbootecommerce.shophappens.cart.application.port.in.MergeGuestCartUseCase;
import com.springbootecommerce.shophappens.cart.application.port.out.CartMergeLedger;
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
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.TestingAuthenticationToken;

@Import(GuestCartMergeRecoveryIT.GuestCartStoreConfiguration.class)
class GuestCartMergeRecoveryIT extends AbstractIntegrationTest {
    private static final CustomerReference CUSTOMER = new CustomerReference(9_000_001L);
    private static final ProductId PRODUCT = new ProductId(9_000_001L);

    @Autowired MergeGuestCartUseCase mergeGuestCart;
    @Autowired CustomerCartQuery customerCarts;
    @Autowired CartMergeLedger ledger;
    @Autowired ControllableGuestCartStore guests;

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
                            assertThat(item.product()).isEqualTo(new ProductId(PRODUCT.value()));
                            assertThat(item.quantity()).isEqualTo(3);
                        });

        authenticate(handler, session, authentication);

        assertThat(session.getAttribute(GuestCartReference.SESSION_ATTRIBUTE)).isNull();
        assertThat(guests.find(guestId)).isEmpty();
        assertThat(customerCarts.get(new CustomerId(CUSTOMER.value())).items())
                .singleElement()
                .satisfies(item -> assertThat(item.quantity()).isEqualTo(3));
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
        cart.changeQuantity(PRODUCT, new Quantity(quantity));
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

        @Override
        public Optional<Cart> find(GuestCartId id) {
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
    }
}
