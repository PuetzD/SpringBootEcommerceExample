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
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        Object stored = request.getSession().getAttribute(GuestCartReference.SESSION_ATTRIBUTE);
        if (stored instanceof String guestUuid) {
            try {
                if (authentication.getPrincipal()
                        instanceof AuthenticatedAccountIdentity identity) {
                    customers
                            .findByExternalAccountId(
                                    new ExternalAccountId(identity.account().value()))
                            .ifPresent(
                                    customer ->
                                            mergeGuestCart.merge(
                                                    new GuestCartReference(
                                                            UUID.fromString(guestUuid)),
                                                    customer));
                }
            } catch (RuntimeException ex) {
                log.debug(
                        "Could not merge guest cart after login; guest cart will be dropped.", ex);
            }
            request.getSession().removeAttribute(GuestCartReference.SESSION_ATTRIBUTE);
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
