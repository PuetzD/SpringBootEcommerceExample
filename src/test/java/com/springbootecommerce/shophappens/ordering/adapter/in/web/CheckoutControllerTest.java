package com.springbootecommerce.shophappens.ordering.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

import com.springbootecommerce.shophappens.customer.application.port.in.CurrentCustomerIdentity;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.ordering.application.exception.CheckoutAddressUnavailableException;
import com.springbootecommerce.shophappens.ordering.application.exception.CheckoutItemUnavailableException;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutItem;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutPreparation;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutReview;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutReviewChangedException;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderReference;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlaceOrderCommand;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlaceOrderUseCase;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlacedOrder;
import com.springbootecommerce.shophappens.ordering.application.port.in.PrepareCheckoutUseCase;
import com.springbootecommerce.shophappens.security.SecurityConfiguration;
import com.springbootecommerce.shophappens.security.service.CartMergingAuthenticationSuccessHandler;
import com.springbootecommerce.shophappens.shared.web.CanonicalUrlFactory;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
    @MockitoBean CurrentCustomerIdentity currentCustomer;
    @MockitoBean CartMergingAuthenticationSuccessHandler successHandler;
    @MockitoBean CheckoutReviewSession reviews;
    @MockitoBean Clock clock;
    private static final CustomerReference CUSTOMER = new CustomerReference(42L);
    private static final Instant NOW = Instant.parse("2026-09-09T12:00:00Z");

    @BeforeEach
    void setUp() {
        when(currentCustomer.current()).thenReturn(Optional.of(CUSTOMER));
        when(clock.instant()).thenReturn(NOW);
        when(preparation.prepare(new CustomerId(CUSTOMER.value()))).thenReturn(prepared(List.of()));
    }

    @Test
    void anonymousCheckoutRedirectsToLogin() throws Exception {
        when(currentCustomer.current()).thenReturn(Optional.empty());
        mvc.perform(get("/checkout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void checkoutShowsSelectedFactsAndMerchandiseTotal() throws Exception {
        mvc.perform(get("/checkout").with(user("customer").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Blue shirt")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("BLUE")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("40.00 EUR")));
        verify(reviews).put(any(), any(), any(CheckoutReview.class));
    }

    @Test
    void authenticatedCheckoutRendersFormAndPostsWithCsrf() throws Exception {
        UUID checkoutId = UUID.randomUUID();
        CheckoutReview review =
                new CheckoutReview(
                        new CustomerId(42), prepared(List.of()).items(), NOW.plusSeconds(900));
        when(reviews.find(any(), any())).thenReturn(Optional.of(review));
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
        ArgumentCaptor<PlaceOrderCommand> command =
                ArgumentCaptor.forClass(PlaceOrderCommand.class);
        verify(orders).place(command.capture());
        org.assertj.core.api.Assertions.assertThat(command.getValue().review()).isEqualTo(review);
        verify(reviews).remove(any(), org.mockito.ArgumentMatchers.eq(checkoutId));
    }

    @Test
    void orderingAddressFailureReturnsForbidden() throws Exception {
        when(orders.place(any()))
                .thenThrow(
                        new CheckoutAddressUnavailableException(
                                "Address is unavailable", new IllegalStateException("customer")));

        submitValidCheckout().andExpect(status().isForbidden());
    }

    @Test
    void orderingItemFailureRendersCheckoutAvailabilityMessage() throws Exception {
        when(orders.place(any()))
                .thenThrow(
                        new CheckoutItemUnavailableException(
                                "Item is unavailable", new IllegalStateException("catalog")));

        submitValidCheckout()
                .andExpect(status().isOk())
                .andExpect(view().name("ordering/checkout"))
                .andExpect(
                        model().attribute("checkoutError", "Some items are no longer available."));
    }

    @Test
    void changedReviewRendersFreshFactsAndRequiresAnotherSubmission() throws Exception {
        when(orders.place(any())).thenThrow(new CheckoutReviewChangedException());

        submitValidCheckout()
                .andExpect(status().isOk())
                .andExpect(view().name("ordering/checkout"))
                .andExpect(model().attributeExists("checkoutError"));
        verify(reviews).put(any(), any(), any(CheckoutReview.class));
    }

    @Test
    void invalidAddressFormStoresTheFreshlyRenderedReview() throws Exception {
        UUID checkout = UUID.randomUUID();

        mvc.perform(
                        post("/checkout")
                                .with(user("customer").roles("CUSTOMER"))
                                .with(csrf())
                                .param("checkoutId", checkout.toString())
                                .param("billingAddressId", "12"))
                .andExpect(status().isOk())
                .andExpect(view().name("ordering/checkout"));
        verify(reviews)
                .put(any(), org.mockito.ArgumentMatchers.eq(checkout), any(CheckoutReview.class));
    }

    @Test
    void unavailableVariantDisablesPlacementAndLinksBackToCart() throws Exception {
        when(preparation.prepare(new CustomerId(CUSTOMER.value())))
                .thenReturn(prepared(List.of(new ProductVariantId(203))));

        mvc.perform(get("/checkout").with(user("customer").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("disabled")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/cart")));
        verify(reviews, never()).put(any(), any(), any());
    }

    private org.springframework.test.web.servlet.ResultActions submitValidCheckout()
            throws Exception {
        return mvc.perform(
                post("/checkout")
                        .with(user("customer").roles("CUSTOMER"))
                        .with(csrf())
                        .param("checkoutId", UUID.randomUUID().toString())
                        .param("shippingAddressId", "11")
                        .param("billingAddressId", "12"));
    }

    private static CheckoutPreparation prepared(List<ProductVariantId> unavailable) {
        return new CheckoutPreparation(
                new CustomerId(42),
                List.of(
                        new CheckoutItem(
                                new ProductVariantId(202),
                                new ProductId(7),
                                "BLUE",
                                "Blue shirt",
                                new Money(new BigDecimal("20.00")),
                                2)),
                List.of(),
                unavailable);
    }
}
