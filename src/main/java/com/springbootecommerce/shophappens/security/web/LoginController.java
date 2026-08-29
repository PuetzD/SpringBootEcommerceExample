package com.springbootecommerce.shophappens.security.web;

import com.springbootecommerce.shophappens.shared.web.CanonicalUrlFactory;
import com.springbootecommerce.shophappens.shared.web.SeoMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class LoginController {
    private final CanonicalUrlFactory canonicalUrlFactory;

    @GetMapping("/login")
    public String login(Model model) {
        var seo =
                new SeoMetadata(
                        "Sign in", "Sign in to your E-Shop account.", "/login", "noindex,follow");

        model.addAttribute("seo", seo);
        model.addAttribute("canonicalUrl", canonicalUrlFactory.forPath(seo.canonicalPath()));
        return "login";
    }
}
