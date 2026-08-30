package com.springbootecommerce.shophappens.ordering.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.springbootecommerce.shophappens.account.application.port.in.AccountReference;
import com.springbootecommerce.shophappens.account.application.port.in.AuthenticatedAccountIdentity;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReferenceQuery;
import com.springbootecommerce.shophappens.customer.application.port.in.ExternalAccountId;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutPreparation;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderReference;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlaceOrderUseCase;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlacedOrder;
import com.springbootecommerce.shophappens.ordering.application.port.in.PrepareCheckoutUseCase;
import com.springbootecommerce.shophappens.security.SecurityConfiguration;
import com.springbootecommerce.shophappens.security.service.CartMergingAuthenticationSuccessHandler;
import com.springbootecommerce.shophappens.shared.web.CanonicalUrlFactory;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CheckoutController.class)
@Import({CanonicalUrlFactory.class, SecurityConfiguration.class})
class CheckoutControllerTest {
    @Autowired MockMvc mvc;

    @MockitoBean PrepareCheckoutUseCase preparation;
    @MockitoBean PlaceOrderUseCase orders;
    @MockitoBean AuthenticatedAccountIdentity authenticatedAccount;
    @MockitoBean CustomerReferenceQuery customers;
    @MockitoBean CartMergingAuthenticationSuccessHandler successHandler;

    @BeforeEach
    void authenticatedCustomer() {
        when(authenticatedAccount.account()).thenReturn(new AccountReference(7L));
        when(customers.findByExternalAccountId(new ExternalAccountId(7L)))
                .thenReturn(java.util.Optional.of(new CustomerReference(42L)));
        when(preparation.prepare(new CustomerReference(42L)))
                .thenReturn(
                        new CheckoutPreparation(new CustomerReference(42L), List.of(), List.of()));
    }

    @Test
    void anonymousCheckoutRedirectsToLogin() throws Exception {
        mvc.perform(get("/checkout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void authenticatedCheckoutRendersFormAndPostsWithCsrf() throws Exception {
        UUID checkoutId = UUID.randomUUID();
        when(orders.place(any()))
                .thenReturn(
                        new PlacedOrder(
                                new OrderReference(UUID.randomUUID()),
                                "ORD-20260830-ABCDEF123456",
                                new Money(new BigDecimal("12.00")),
                                Instant.parse("2026-08-30T08:00:00Z")));

        mvc.perform(get("/checkout").with(user("customer").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(view().name("ordering/checkout"));

        mvc.perform(
                        post("/checkout")
                                .with(user("customer").roles("CUSTOMER"))
                                .with(csrf())
                                .param("checkoutId", checkoutId.toString())
                                .param("shippingAddressId", "11")
                                .param("billingAddressId", "12"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/ORD-20260830-ABCDEF123456"));
    }
}
