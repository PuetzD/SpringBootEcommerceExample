package com.springbootecommerce.shophappens.ordering.adapter.in.web;

import com.springbootecommerce.shophappens.catalog.application.port.in.PublishedInsufficientStockException;
import com.springbootecommerce.shophappens.catalog.application.port.in.PublishedProductUnavailableException;
import com.springbootecommerce.shophappens.customer.application.port.in.CurrentCustomerIdentity;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.customer.application.port.in.OwnedAddressUnavailableException;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutPreparation;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlaceOrderCommand;
import com.springbootecommerce.shophappens.ordering.application.port.in.PlaceOrderUseCase;
import com.springbootecommerce.shophappens.ordering.application.port.in.PrepareCheckoutUseCase;
import com.springbootecommerce.shophappens.ordering.domain.exception.EmptyCheckoutException;
import com.springbootecommerce.shophappens.shared.web.CanonicalUrlFactory;
import com.springbootecommerce.shophappens.shared.web.SeoMetadata;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @GetMapping
    public String form(Model model) {
        CustomerReference customer = currentCustomerOrThrow();
        addModel(model, customer, new CheckoutForm());
        return "ordering/checkout";
    }

    @PostMapping
    public String place(
            @Valid @ModelAttribute("checkoutForm") CheckoutForm form,
            BindingResult bindingResult,
            Model model) {
        CustomerReference customer = currentCustomerOrThrow();
        if (bindingResult.hasErrors()) {
            addModel(model, customer, form);
            return "ordering/checkout";
        }
        var result =
                orders.place(
                        new PlaceOrderCommand(
                                customer,
                                new com.springbootecommerce.shophappens.ordering.application.port.in
                                        .CheckoutReference(form.getCheckoutId()),
                                new com.springbootecommerce.shophappens.customer.application.port.in
                                        .AddressReference(form.getShippingAddressId()),
                                new com.springbootecommerce.shophappens.customer.application.port.in
                                        .AddressReference(form.getBillingAddressId())));
        return "redirect:/orders/" + result.orderNumber();
    }

    @ExceptionHandler(OwnedAddressUnavailableException.class)
    public String addressNotOwned(OwnedAddressUnavailableException exception) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Address is not owned", exception);
    }

    @ExceptionHandler({
        EmptyCheckoutException.class,
        PublishedProductUnavailableException.class,
        PublishedInsufficientStockException.class
    })
    public String checkoutFailure(Model model) {
        CustomerReference customer = currentCustomerOrThrow();
        addModel(model, customer, new CheckoutForm());
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

    private void addModel(Model model, CustomerReference customer, CheckoutForm form) {
        CheckoutPreparation result = preparation.prepare(customer);
        if (form.getCheckoutId() == null) {
            form.setCheckoutId(UUID.randomUUID());
        }
        if (form.getShippingAddressId() == null) {
            result.addresses().stream()
                    .filter(address -> address.defaultShipping())
                    .findFirst()
                    .ifPresent(address -> form.setShippingAddressId(address.address().value()));
        }
        if (form.getBillingAddressId() == null) {
            result.addresses().stream()
                    .filter(address -> address.defaultBilling())
                    .findFirst()
                    .ifPresent(address -> form.setBillingAddressId(address.address().value()));
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
