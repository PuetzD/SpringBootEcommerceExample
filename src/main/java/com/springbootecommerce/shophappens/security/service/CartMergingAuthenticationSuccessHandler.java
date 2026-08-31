package com.springbootecommerce.shophappens.security.service;

import com.springbootecommerce.shophappens.account.application.port.in.AuthenticatedAccountIdentity;
import com.springbootecommerce.shophappens.cart.application.port.in.GuestCartReference;
import com.springbootecommerce.shophappens.cart.application.port.in.MergeGuestCartUseCase;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReferenceQuery;
import com.springbootecommerce.shophappens.customer.application.port.in.ExternalAccountId;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CartMergingAuthenticationSuccessHandler
        extends SavedRequestAwareAuthenticationSuccessHandler {

    private final MergeGuestCartUseCase mergeGuestCart;
    private final CustomerReferenceQuery customers;

    public CartMergingAuthenticationSuccessHandler(
            MergeGuestCartUseCase mergeGuestCart, CustomerReferenceQuery customers) {
        this.mergeGuestCart = mergeGuestCart;
        this.customers = customers;
        setDefaultTargetUrl("/");
    }

    @Override
    public void onAuthenticationSuccess(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Authentication authentication)
            throws IOException, ServletException {
        Object stored = request.getSession().getAttribute(GuestCartReference.SESSION_ATTRIBUTE);
        if (stored == null) {
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }
        if (!(stored instanceof String guestUuid)) {
            request.getSession().removeAttribute(GuestCartReference.SESSION_ATTRIBUTE);
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }
        {
            GuestCartReference guest;
            try {
                guest = new GuestCartReference(UUID.fromString(guestUuid));
            } catch (IllegalArgumentException malformed) {
                request.getSession().removeAttribute(GuestCartReference.SESSION_ATTRIBUTE);
                super.onAuthenticationSuccess(request, response, authentication);
                return;
            }
            try {
                if (authentication.getPrincipal()
                        instanceof AuthenticatedAccountIdentity identity) {
                    customers
                            .findByExternalAccountId(
                                    new ExternalAccountId(identity.account().value()))
                            .ifPresent(
                                    customer -> {
                                        mergeGuestCart.merge(guest, customer);
                                        request.getSession()
                                                .removeAttribute(
                                                        GuestCartReference.SESSION_ATTRIBUTE);
                                    });
                }
            } catch (RuntimeException ex) {
                log.warn("Could not merge guest cart after login; guest cart will be dropped.", ex);
            }
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
