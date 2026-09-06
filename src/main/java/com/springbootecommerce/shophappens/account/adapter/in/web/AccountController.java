package com.springbootecommerce.shophappens.account.adapter.in.web;

import com.springbootecommerce.shophappens.customer.application.port.in.CurrentCustomerIdentity;
import com.springbootecommerce.shophappens.shared.web.CanonicalUrlFactory;
import com.springbootecommerce.shophappens.shared.web.SeoMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequiredArgsConstructor
public class AccountController {
    private final CurrentCustomerIdentity currentCustomer;
    private final CanonicalUrlFactory canonicalUrlFactory;

    @GetMapping("/account")
    public String overview(Model model) {
        currentCustomer
                .current()
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "Customer not found"));

        var seo =
                new SeoMetadata(
                        "Your account",
                        "Manage your E-Shop account, addresses, and orders.",
                        "/account",
                        "noindex,nofollow");
        model.addAttribute("seo", seo);
        model.addAttribute("canonicalUrl", canonicalUrlFactory.forPath(seo.canonicalPath()));
        return "account/overview";
    }
}
