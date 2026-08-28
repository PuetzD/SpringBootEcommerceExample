package com.springbootecommerce.shophappens.account.adapter.in.web;

import com.springbootecommerce.shophappens.account.application.EmailAlreadyRegisteredException;
import com.springbootecommerce.shophappens.account.application.port.in.RegisterCustomerAccount;
import com.springbootecommerce.shophappens.account.application.port.in.RegisterCustomerAccountUseCase;
import com.springbootecommerce.shophappens.web.support.CanonicalUrlFactory;
import com.springbootecommerce.shophappens.web.support.SeoMetadata;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@Controller
public class RegistrationController {

    private final RegisterCustomerAccountUseCase registration;
    private final CanonicalUrlFactory canonicalUrlFactory;

    @GetMapping("/register")
    public String registrationForm(Model model) {
        addSeo(model);
        if (!model.containsAttribute("registrationForm")) {
            model.addAttribute("registrationForm", new RegistrationForm());
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registrationForm") RegistrationForm form,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            addSeo(model);
            return "register";
        }
        try {
            registration.register(new RegisterCustomerAccount(form.getEmail(), form.getPassword()));
        } catch (EmailAlreadyRegisteredException ex) {
            bindingResult.rejectValue(
                    "email", "email.registered", "An account with this email already exists");
            addSeo(model);
            return "register";
        }
        return "redirect:/login?registered";
    }

    private void addSeo(Model model) {
        var seo =
                new SeoMetadata(
                        "Create account",
                        "Create your E-Shop account.",
                        "/register",
                        "noindex,follow");
        model.addAttribute("seo", seo);
        model.addAttribute("canonicalUrl", canonicalUrlFactory.forPath(seo.canonicalPath()));
    }
}
