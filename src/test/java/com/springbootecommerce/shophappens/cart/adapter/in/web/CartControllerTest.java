package com.springbootecommerce.shophappens.cart.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.springbootecommerce.shophappens.cart.application.port.in.CartItemSnapshot;
import com.springbootecommerce.shophappens.cart.application.port.in.CustomerCartSnapshot;
import com.springbootecommerce.shophappens.cart.application.port.in.CustomerCartUseCase;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartReference;
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
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
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

    @Test
    void anonymousPostToCartItemsCreatesGuestCartInSessionAndInvokesGuestUseCase()
            throws Exception {
        when(currentCustomer.current()).thenReturn(Optional.empty());

        MockHttpSession session = new MockHttpSession();
        mvc.perform(
                        post("/cart/items")
                                .session(session)
                                .with(csrf())
                                .param("product", "3")
                                .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        Object stored = session.getAttribute(GuestCartReference.SESSION_ATTRIBUTE);
        org.assertj.core.api.Assertions.assertThat(stored).isNotNull();

        ArgumentCaptor<GuestCartReference> guest =
                ArgumentCaptor.forClass(GuestCartReference.class);
        verify(guestCart)
                .changeQuantity(guest.capture(), eq(new ProductId(PRODUCT.value())), eq(2));
        org.assertj.core.api.Assertions.assertThat(guest.getValue().value().toString())
                .isEqualTo(stored);
    }

    @Test
    void authenticatedPostToCartItemsResolvesCustomerAndInvokesCustomerUseCase() throws Exception {
        when(currentCustomer.current()).thenReturn(Optional.of(CUSTOMER));

        mvc.perform(
                        post("/cart/items")
                                .with(user("alex").roles("CUSTOMER"))
                                .with(csrf())
                                .param("product", "3")
                                .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(customerCart)
                .changeQuantity(
                        new CustomerId(CUSTOMER.value()), new ProductId(PRODUCT.value()), 2);
        verify(guestCart, never())
                .changeQuantity(any(), any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void anonymousCanRemoveFromGuestCart() throws Exception {
        when(currentCustomer.current()).thenReturn(Optional.empty());

        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/cart/items/3/remove").session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        ArgumentCaptor<GuestCartReference> guest =
                ArgumentCaptor.forClass(GuestCartReference.class);
        verify(guestCart).remove(guest.capture(), eq(new ProductId(PRODUCT.value())));
    }

    @Test
    void rendersTheCartViewForAnAuthenticatedCustomer() throws Exception {
        when(currentCustomer.current()).thenReturn(Optional.of(CUSTOMER));
        when(customerCart.getSnapshot(new CustomerId(CUSTOMER.value())))
                .thenReturn(
                        new CustomerCartSnapshot(
                                new CustomerId(CUSTOMER.value()),
                                List.of(new CartItemSnapshot(new ProductId(PRODUCT.value()), 2))));
        when(catalog.findActiveById(PRODUCT)).thenReturn(Optional.of(productSummary()));

        mvc.perform(get("/cart").with(user("alex").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(view().name("cart/detail"));
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
    void requiresCsrfForCartMutations() throws Exception {
        mvc.perform(post("/cart/items").param("product", "3").param("quantity", "2"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/cart/items/3/remove")).andExpect(status().isForbidden());
    }

    private static ProductSummary productSummary() {
        return new ProductSummary(
                PRODUCT,
                "WEAP-003",
                "Rubber Duck of Debugging",
                "Descriptive text",
                new Money(new BigDecimal("18.99")),
                10,
                "/images/product-placeholder.svg");
    }
}
