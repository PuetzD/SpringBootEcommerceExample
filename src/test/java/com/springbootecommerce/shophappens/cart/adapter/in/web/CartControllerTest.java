package com.springbootecommerce.shophappens.cart.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.springbootecommerce.shophappens.cart.application.port.in.CartItemSnapshot;
import com.springbootecommerce.shophappens.cart.application.port.in.CustomerCartSnapshot;
import com.springbootecommerce.shophappens.cart.application.port.in.CustomerCartUseCase;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartConsumedException;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartReference;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartSnapshot;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.BrowseCatalogUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductSummary;
import com.springbootecommerce.shophappens.customer.application.port.in.CurrentCustomerIdentity;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.security.SecurityConfiguration;
import com.springbootecommerce.shophappens.security.service.CartMergingAuthenticationSuccessHandler;
import com.springbootecommerce.shophappens.shared.web.CanonicalUrlFactory;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CartController.class)
@Import({CanonicalUrlFactory.class, SecurityConfiguration.class, GuestCartSession.class})
class CartControllerTest {
    @Autowired MockMvc mvc;

    @MockitoBean CurrentCustomerIdentity currentCustomer;
    @MockitoBean GuestCartUseCase guestCart;
    @MockitoBean CustomerCartUseCase customerCart;
    @MockitoBean BrowseCatalogUseCase catalog;
    @MockitoBean CartMergingAuthenticationSuccessHandler successHandler;

    private static final CustomerReference CUSTOMER = new CustomerReference(7L);
    private static final ProductReference PRODUCT = new ProductReference(3L);
    private static final ProductVariantId VARIANT = new ProductVariantId(202L);

    @Test
    void anonymousPostToCartItemsCreatesGuestCartInSessionAndInvokesGuestUseCase()
            throws Exception {
        when(currentCustomer.current()).thenReturn(Optional.empty());
        when(catalog.findActiveByVariantId(VARIANT)).thenReturn(Optional.of(productSummary()));

        MockHttpSession session = new MockHttpSession();
        mvc.perform(
                        post("/cart/items")
                                .session(session)
                                .with(csrf())
                                .param("variant", "202")
                                .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        Object stored = session.getAttribute(GuestCartReference.SESSION_ATTRIBUTE);
        org.assertj.core.api.Assertions.assertThat(stored).isNotNull();

        ArgumentCaptor<GuestCartReference> guest =
                ArgumentCaptor.forClass(GuestCartReference.class);
        verify(guestCart).add(guest.capture(), eq(VARIANT), eq(2));
        org.assertj.core.api.Assertions.assertThat(guest.getValue().value().toString())
                .isEqualTo(stored);
    }

    @Test
    void authenticatedPostToCartItemsResolvesCustomerAndInvokesCustomerUseCase() throws Exception {
        when(currentCustomer.current()).thenReturn(Optional.of(CUSTOMER));
        when(catalog.findActiveByVariantId(VARIANT)).thenReturn(Optional.of(productSummary()));

        mvc.perform(
                        post("/cart/items")
                                .with(user("alex").roles("CUSTOMER"))
                                .with(csrf())
                                .param("variant", "202")
                                .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(customerCart).add(new CustomerId(CUSTOMER.value()), VARIANT, 2);
        verify(guestCart, never()).add(any(), any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void addingUnavailableOrInvalidVariantFailsWithoutChangingCart() throws Exception {
        mvc.perform(post("/cart/items").with(csrf()).param("variant", "303").param("quantity", "1"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/cart/items").with(csrf()).param("variant", "0").param("quantity", "1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(customerCart, guestCart);
    }

    @Test
    void additiveOverflowReturnsBadRequest() throws Exception {
        when(currentCustomer.current()).thenReturn(Optional.of(CUSTOMER));
        when(catalog.findActiveByVariantId(VARIANT)).thenReturn(Optional.of(productSummary()));
        doThrow(new IllegalArgumentException("Quantity must be between 1 and 999"))
                .when(customerCart)
                .add(new CustomerId(CUSTOMER.value()), VARIANT, 1);

        mvc.perform(
                        post("/cart/items")
                                .with(user("alex").roles("CUSTOMER"))
                                .with(csrf())
                                .param("variant", "202")
                                .param("quantity", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anonymousCanRemoveFromGuestCart() throws Exception {
        when(currentCustomer.current()).thenReturn(Optional.empty());

        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/cart/items/202/remove").session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        ArgumentCaptor<GuestCartReference> guest =
                ArgumentCaptor.forClass(GuestCartReference.class);
        verify(guestCart).remove(guest.capture(), eq(VARIANT));
        verifyNoInteractions(catalog);
    }

    @Test
    void unavailableStoredVariantCanBeRemovedWithoutCatalog() throws Exception {
        when(currentCustomer.current()).thenReturn(Optional.of(CUSTOMER));

        mvc.perform(
                        post("/cart/items/202/remove")
                                .with(user("alex").roles("CUSTOMER"))
                                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(customerCart).remove(new CustomerId(CUSTOMER.value()), VARIANT);
        verifyNoInteractions(catalog);
    }

    @Test
    void explicitLineUpdateKeepsSelectedIdentity() throws Exception {
        when(currentCustomer.current()).thenReturn(Optional.of(CUSTOMER));

        mvc.perform(
                        post("/cart/items/202/quantity")
                                .with(user("alex").roles("CUSTOMER"))
                                .with(csrf())
                                .param("quantity", "4"))
                .andExpect(status().is3xxRedirection());

        verify(customerCart).changeQuantity(new CustomerId(CUSTOMER.value()), VARIANT, 4);
        verifyNoInteractions(catalog);
    }

    @Test
    void guestLineUpdateKeepsSelectedIdentity() throws Exception {
        when(currentCustomer.current()).thenReturn(Optional.empty());
        MockHttpSession session = new MockHttpSession();

        mvc.perform(
                        post("/cart/items/202/quantity")
                                .session(session)
                                .with(csrf())
                                .param("quantity", "4"))
                .andExpect(status().is3xxRedirection());

        verify(guestCart).changeQuantity(any(GuestCartReference.class), eq(VARIANT), eq(4));
        verifyNoInteractions(catalog);
    }

    @Test
    void rendersTheCartViewForAnAuthenticatedCustomer() throws Exception {
        when(currentCustomer.current()).thenReturn(Optional.of(CUSTOMER));
        when(customerCart.getSnapshot(new CustomerId(CUSTOMER.value())))
                .thenReturn(
                        new CustomerCartSnapshot(
                                new CustomerId(CUSTOMER.value()),
                                List.of(
                                        new CartItemSnapshot(VARIANT, 2),
                                        new CartItemSnapshot(new ProductVariantId(303), 1))));
        when(catalog.findActiveByVariantId(VARIANT)).thenReturn(Optional.of(productSummary()));

        mvc.perform(get("/cart").with(user("alex").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(view().name("cart/detail"))
                .andExpect(model().attribute("cartEmpty", false))
                .andExpect(model().attribute("checkoutAllowed", false))
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.containsString(
                                                "This item is no longer available")));
    }

    @Test
    void fullyAvailableCustomerCartOffersCheckoutAndExactVariantActions() throws Exception {
        when(currentCustomer.current()).thenReturn(Optional.of(CUSTOMER));
        when(customerCart.getSnapshot(new CustomerId(CUSTOMER.value())))
                .thenReturn(
                        new CustomerCartSnapshot(
                                new CustomerId(CUSTOMER.value()),
                                List.of(new CartItemSnapshot(VARIANT, 2))));
        when(catalog.findActiveByVariantId(VARIANT)).thenReturn(Optional.of(productSummary()));

        mvc.perform(get("/cart").with(user("alex").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("checkoutAllowed", true))
                .andExpect(
                        content()
                                .string(org.hamcrest.Matchers.containsString("href=\"/checkout\"")))
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.containsString(
                                                "action=\"/cart/items/202/quantity\"")))
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.containsString(
                                                "action=\"/cart/items/202/remove\"")));
    }

    @Test
    void rendersEmptyCartForAnonymousWithoutGuestUuid() throws Exception {
        when(currentCustomer.current()).thenReturn(Optional.empty());

        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/cart").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("cart/detail"));
    }

    @Test
    void guestCartAlsoRendersAndRetainsUnavailableLines() throws Exception {
        when(currentCustomer.current()).thenReturn(Optional.empty());
        var reference = new GuestCartReference(UUID.randomUUID());
        var session = new MockHttpSession();
        session.setAttribute(GuestCartReference.SESSION_ATTRIBUTE, reference.value().toString());
        when(guestCart.getSnapshot(reference))
                .thenReturn(
                        new GuestCartSnapshot(
                                reference,
                                List.of(
                                        new CartItemSnapshot(VARIANT, 2),
                                        new CartItemSnapshot(new ProductVariantId(303), 1))));
        when(catalog.findActiveByVariantId(VARIANT)).thenReturn(Optional.of(productSummary()));

        mvc.perform(get("/cart").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("cartEmpty", false))
                .andExpect(model().attribute("checkoutAllowed", false))
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.containsString(
                                                "This item is no longer available")))
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.containsString(
                                                "/cart/items/303/remove")));
    }

    @Test
    void requiresCsrfForCartMutations() throws Exception {
        mvc.perform(post("/cart/items").param("variant", "202").param("quantity", "2"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/cart/items/202/quantity").param("quantity", "2"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/cart/items/202/remove")).andExpect(status().isForbidden());
    }

    @Test
    void consumedGuestMutationReturnsConflictAndRecoveryLink() throws Exception {
        when(currentCustomer.current()).thenReturn(Optional.empty());
        MockHttpSession session = new MockHttpSession();
        doThrow(new GuestCartConsumedException())
                .when(guestCart)
                .remove(any(GuestCartReference.class), eq(VARIANT));

        mvc.perform(post("/cart/items/202/remove").session(session).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(view().name("cart/conflict"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/cart")))
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.containsString(
                                                "already been merged")));
        org.assertj.core.api.Assertions.assertThat(
                        session.getAttribute(GuestCartReference.SESSION_ATTRIBUTE))
                .isNotNull();
    }

    @Test
    void guestStorageFailureReturnsServiceUnavailableWithReloadGuidance() throws Exception {
        when(currentCustomer.current()).thenReturn(Optional.empty());
        doThrow(new DataAccessResourceFailureException("Redis unavailable"))
                .when(guestCart)
                .remove(any(GuestCartReference.class), eq(VARIANT));

        mvc.perform(post("/cart/items/202/remove").with(csrf()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(view().name("cart/conflict"))
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.containsString(
                                                "Cart could not be saved")));
    }

    private static ProductSummary productSummary() {
        return new ProductSummary(
                PRODUCT,
                "WEAP-003",
                "Rubber Duck of Debugging",
                "Descriptive text",
                new Money(new BigDecimal("18.99")),
                10,
                "/images/product-placeholder.svg",
                VARIANT);
    }
}
