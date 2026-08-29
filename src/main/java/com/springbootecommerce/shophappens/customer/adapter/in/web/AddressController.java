package com.springbootecommerce.shophappens.customer.adapter.in.web;

import com.springbootecommerce.shophappens.customer.application.CustomerNotFoundException;
import com.springbootecommerce.shophappens.customer.application.port.in.AddressReference;
import com.springbootecommerce.shophappens.customer.application.port.in.AddressSnapshot;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.customer.application.port.in.ManageCustomerAddressesUseCase;
import com.springbootecommerce.shophappens.customer.application.port.in.ManageCustomerAddressesUseCase.SaveAddressCommand;
import com.springbootecommerce.shophappens.customer.application.port.in.OwnedAddressQuery;
import com.springbootecommerce.shophappens.customer.domain.exception.AddressNotOwnedException;
import com.springbootecommerce.shophappens.shared.web.CanonicalUrlFactory;
import com.springbootecommerce.shophappens.shared.web.SeoMetadata;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Controller
@RequestMapping("/account/addresses")
public class AddressController {

    private final AuthenticatedCustomerResolver authenticator;
    private final OwnedAddressQuery addresses;
    private final ManageCustomerAddressesUseCase manager;
    private final CanonicalUrlFactory canonicalUrlFactory;

    @GetMapping
    public String list(Model model) {
        CustomerReference customer = currentCustomer();
        addSeo(model, "Your addresses", "/account/addresses");
        model.addAttribute("customer", customer);
        model.addAttribute("addresses", addresses.findForCustomer(customer));
        return "customer/address-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        prepareForm(model, "Add an address", "/account/addresses/new", new AddressForm());
        return "customer/address-form";
    }

    @PostMapping("/new")
    public String create(
            @Valid @ModelAttribute("addressForm") AddressForm form,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, "Add an address", "/account/addresses/new", form);
            return "customer/address-form";
        }
        CustomerReference customer = currentCustomer();
        manager.save(customer, toCommand(null, form));
        return "redirect:/account/addresses";
    }

    @GetMapping("/{addressId}/edit")
    public String editForm(@PathVariable long addressId, Model model) {
        CustomerReference customer = currentCustomer();
        AddressSnapshot snapshot = addresses.getOwned(customer, new AddressReference(addressId));
        prepareForm(
                model,
                "Edit an address",
                "/account/addresses/" + addressId + "/edit",
                AddressForm.from(snapshot));
        return "customer/address-form";
    }

    @PostMapping("/{addressId}/edit")
    public String edit(
            @PathVariable long addressId,
            @Valid @ModelAttribute("addressForm") AddressForm form,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            prepareForm(
                    model, "Edit an address", "/account/addresses/" + addressId + "/edit", form);
            return "customer/address-form";
        }
        CustomerReference customer = currentCustomer();
        manager.save(customer, toCommand(new AddressReference(addressId), form));
        return "redirect:/account/addresses";
    }

    @PostMapping("/{addressId}/default-shipping")
    public String defaultShipping(@PathVariable long addressId) {
        CustomerReference customer = currentCustomer();
        AddressSnapshot snapshot = addresses.getOwned(customer, new AddressReference(addressId));
        manager.save(customer, preserveCommand(snapshot, true, snapshot.defaultBilling()));
        return "redirect:/account/addresses";
    }

    @PostMapping("/{addressId}/default-billing")
    public String defaultBilling(@PathVariable long addressId) {
        CustomerReference customer = currentCustomer();
        AddressSnapshot snapshot = addresses.getOwned(customer, new AddressReference(addressId));
        manager.save(customer, preserveCommand(snapshot, snapshot.defaultShipping(), true));
        return "redirect:/account/addresses";
    }

    @PostMapping("/{addressId}/delete")
    public String delete(@PathVariable long addressId) {
        CustomerReference customer = currentCustomer();
        manager.remove(customer, new AddressReference(addressId));
        return "redirect:/account/addresses";
    }

    @ExceptionHandler({AddressNotOwnedException.class, CustomerNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void notFound() {}

    private CustomerReference currentCustomer() {
        return authenticator
                .resolve()
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "Customer not found"));
    }

    private SaveAddressCommand toCommand(AddressReference address, AddressForm form) {
        return new SaveAddressCommand(
                address,
                form.getRecipientName(),
                form.getCompanyName(),
                form.getAddressLine1(),
                form.getAddressLine2(),
                form.getCity(),
                form.getRegion(),
                form.getPostalCode(),
                form.getCountryCode(),
                form.getPhoneNumber(),
                form.isDefaultShipping(),
                form.isDefaultBilling());
    }

    private SaveAddressCommand preserveCommand(
            AddressSnapshot snapshot, boolean defaultShipping, boolean defaultBilling) {
        return new SaveAddressCommand(
                snapshot.address(),
                snapshot.recipientName(),
                snapshot.companyName(),
                snapshot.addressLine1(),
                snapshot.addressLine2(),
                snapshot.city(),
                snapshot.region(),
                snapshot.postalCode(),
                snapshot.countryCode(),
                snapshot.phoneNumber(),
                defaultShipping,
                defaultBilling);
    }

    private void addSeo(Model model, String title, String canonicalPath) {
        var seo =
                new SeoMetadata(
                        title,
                        "Manage your E-Shop delivery and billing addresses.",
                        canonicalPath,
                        "noindex,nofollow");
        model.addAttribute("seo", seo);
        model.addAttribute("canonicalUrl", canonicalUrlFactory.forPath(seo.canonicalPath()));
    }

    private void prepareForm(Model model, String title, String action, AddressForm form) {
        addSeo(model, title, action);
        model.addAttribute("formAction", action);
        model.addAttribute("addressForm", form);
    }
}
