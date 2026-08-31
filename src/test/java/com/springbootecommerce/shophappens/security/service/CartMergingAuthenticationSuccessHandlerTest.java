package com.springbootecommerce.shophappens.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.account.application.port.in.AccountReference;
import com.springbootecommerce.shophappens.account.application.port.in.AuthenticatedAccountIdentity;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartReference;
import com.springbootecommerce.shophappens.cart.application.port.in.MergeGuestCartUseCase;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReferenceQuery;
import com.springbootecommerce.shophappens.customer.application.port.in.ExternalAccountId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;

class CartMergingAuthenticationSuccessHandlerTest {

    private MergeGuestCartUseCase mergeGuestCart;
    private CustomerReferenceQuery customers;
    private CartMergingAuthenticationSuccessHandler handler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockHttpSession session;
    private Authentication authentication;

    private static final CustomerReference CUSTOMER = new CustomerReference(7L);
    private static final UUID GUEST_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mergeGuestCart = mock(MergeGuestCartUseCase.class);
        customers = mock(CustomerReferenceQuery.class);
        handler = new CartMergingAuthenticationSuccessHandler(mergeGuestCart, customers);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        session = new MockHttpSession();
        request.setSession(session);
        authentication = mock(Authentication.class);
        when(authentication.getPrincipal())
                .thenReturn((AuthenticatedAccountIdentity) () -> new AccountReference(5L));
    }

    @Test
    void successfulMergeRemovesGuestCartSessionAttribute() throws Exception {
        session.setAttribute(GuestCartReference.SESSION_ATTRIBUTE, GUEST_UUID.toString());
        when(customers.findByExternalAccountId(new ExternalAccountId(5L)))
                .thenReturn(Optional.of(CUSTOMER));

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(mergeGuestCart).merge(new GuestCartReference(GUEST_UUID), CUSTOMER);
        assertThat(session.getAttribute(GuestCartReference.SESSION_ATTRIBUTE)).isNull();
        assertThat(response.getRedirectedUrl()).isEqualTo("/");
    }

    @Test
    void redirectsToSavedRequestPathWhenOneExistsElseRoot() throws Exception {
        request.setRequestURI("/account/security-test");
        session.setAttribute(
                "SPRING_SECURITY_SAVED_REQUEST",
                new org.springframework.security.web.savedrequest.DefaultSavedRequest(
                        request, null));

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(mergeGuestCart, never()).merge(any(), any());
        assertThat(response.getRedirectedUrl()).endsWith("/account/security-test");
    }

    @Test
    void redirectsToRootWhenNoGuestUuidIsPresentAndNoSavedRequestExists() throws Exception {
        handler.onAuthenticationSuccess(request, response, authentication);

        verify(mergeGuestCart, never()).merge(any(), any());
        assertThat(session.getAttribute(GuestCartReference.SESSION_ATTRIBUTE)).isNull();
        assertThat(response.getRedirectedUrl()).isEqualTo("/");
    }

    @Test
    void mergeFailurePreservesGuestCartSessionAttributeAndStillRedirects() throws Exception {
        session.setAttribute(GuestCartReference.SESSION_ATTRIBUTE, GUEST_UUID.toString());
        when(customers.findByExternalAccountId(new ExternalAccountId(5L)))
                .thenReturn(Optional.of(CUSTOMER));
        doThrow(new IllegalStateException("redis unavailable"))
                .when(mergeGuestCart)
                .merge(any(), any());

        handler.onAuthenticationSuccess(request, response, authentication);

        assertThat(session.getAttribute(GuestCartReference.SESSION_ATTRIBUTE))
                .isEqualTo(GUEST_UUID.toString());
        assertThat(response.getRedirectedUrl()).isEqualTo("/");
    }

    @Test
    void absentCustomerPreservesGuestCartIdentifierForLaterRetry() throws Exception {
        session.setAttribute(GuestCartReference.SESSION_ATTRIBUTE, GUEST_UUID.toString());
        when(customers.findByExternalAccountId(new ExternalAccountId(5L)))
                .thenReturn(Optional.empty());

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(mergeGuestCart, never()).merge(any(), any());
        assertThat(session.getAttribute(GuestCartReference.SESSION_ATTRIBUTE))
                .isEqualTo(GUEST_UUID.toString());
        assertThat(response.getRedirectedUrl()).isEqualTo("/");
    }

    @Test
    void malformedGuestCartIdentifierIsRemovedWithoutCallingMerge() throws Exception {
        session.setAttribute(GuestCartReference.SESSION_ATTRIBUTE, "not-a-uuid");

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(mergeGuestCart, never()).merge(any(), any());
        assertThat(session.getAttribute(GuestCartReference.SESSION_ATTRIBUTE)).isNull();
        assertThat(response.getRedirectedUrl()).isEqualTo("/");
    }
}
