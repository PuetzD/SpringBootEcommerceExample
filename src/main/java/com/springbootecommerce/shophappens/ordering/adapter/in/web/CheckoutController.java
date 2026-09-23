package com.springbootecommerce.shophappens.ordering.adapter.in.web;

import com.springbootecommerce.shophappens.customer.application.port.in.CurrentCustomerIdentity;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutAddressUnavailableException;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutItemUnavailableException;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutPreparation;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutReview;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutReviewChangedException;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlaceOrderCommand;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlaceOrderUseCase;
import com.springbootecommerce.shophappens.ordering.application.port.in.PrepareCheckoutUseCase;
import com.springbootecommerce.shophappens.ordering.domain.exception.EmptyCheckoutException;
import com.springbootecommerce.shophappens.shared.web.CanonicalUrlFactory;
import com.springbootecommerce.shophappens.shared.web.SeoMetadata;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.time.Clock;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/checkout")
public class CheckoutController {
    private final PrepareCheckoutUseCase preparation;
    private final PlaceOrderUseCase orders;
    private final CurrentCustomerIdentity currentCustomer;
    private final CanonicalUrlFactory canonicalUrlFactory;
    private final CheckoutReviewSession reviews;
    private final Clock clock;

    @GetMapping
    public String form(Model model, HttpSession session) {
        CustomerReference customer = currentCustomerOrThrow();
        addModel(model, customer, new CheckoutForm(), session);
        return "ordering/checkout";
    }

    @PostMapping
    public String place(
            @Valid @ModelAttribute("checkoutForm") CheckoutForm form,
            BindingResult bindingResult,
            Model model,
            HttpSession session) {
        CustomerReference customer = currentCustomerOrThrow();
        if (bindingResult.hasErrors()) {
            addModel(model, customer, form, session);
            return "ordering/checkout";
        }
        try {
            var result =
                    orders.place(
                            new PlaceOrderCommand(
                                    new CustomerId(customer.value()),
                                    new com.springbootecommerce.shophappens.ordering.application
                                            .port.in.CheckoutReference(form.getCheckoutId()),
                                    form.getShippingAddressId(),
                                    form.getBillingAddressId(),
                                    reviews.find(session, form.getCheckoutId()).orElse(null)));
            reviews.remove(session, form.getCheckoutId());
            return "redirect:/orders/" + result.orderNumber();
        } catch (CheckoutReviewChangedException exception) {
            addModel(model, customer, form, session);
            model.addAttribute("checkoutError", exception.getMessage());
            return "ordering/checkout";
        }
    }

    @ExceptionHandler(CheckoutAddressUnavailableException.class)
    public ResponseEntity<Void> addressNotOwned() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @ExceptionHandler({EmptyCheckoutException.class, CheckoutItemUnavailableException.class})
    public String checkoutFailure(Model model, HttpSession session) {
        CustomerReference customer = currentCustomerOrThrow();
        addModel(model, customer, new CheckoutForm(), session);
        model.addAttribute("checkoutError", "Some items are no longer available.");
        return "ordering/checkout";
    }

    private CustomerReference currentCustomerOrThrow() {
        return currentCustomer
                .current()
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "Customer not found"));
    }

    private void addModel(
            Model model, CustomerReference customer, CheckoutForm form, HttpSession session) {
        CheckoutPreparation result = preparation.prepare(new CustomerId(customer.value()));
        if (form.getCheckoutId() == null) {
            form.setCheckoutId(UUID.randomUUID());
        }
        if (form.getShippingAddressId() == null) {
            result.addresses().stream()
                    .filter(address -> address.defaultShipping())
                    .findFirst()
                    .ifPresent(address -> form.setShippingAddressId(address.addressId()));
        }
        if (form.getBillingAddressId() == null) {
            result.addresses().stream()
                    .filter(address -> address.defaultBilling())
                    .findFirst()
                    .ifPresent(address -> form.setBillingAddressId(address.addressId()));
        }
        if (!result.items().isEmpty() && result.unavailableVariants().isEmpty()) {
            reviews.put(
                    session,
                    form.getCheckoutId(),
                    new CheckoutReview(
                            result.customer(), result.items(), clock.instant().plusSeconds(900)));
        } else {
            reviews.remove(session, form.getCheckoutId());
        }
        model.addAttribute("checkoutForm", form);
        model.addAttribute("checkout", result);
        var seo =
                new SeoMetadata(
                        "Checkout",
                        "Complete your Shop Happens order.",
                        "/checkout",
                        "noindex,nofollow");
        model.addAttribute("seo", seo);
        model.addAttribute("canonicalUrl", canonicalUrlFactory.forPath(seo.canonicalPath()));
    }
}
