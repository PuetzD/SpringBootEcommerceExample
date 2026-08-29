package com.springbootecommerce.shophappens.security.web;

import com.springbootecommerce.shophappens.shared.web.CanonicalUrlFactory;
import com.springbootecommerce.shophappens.shared.web.SeoMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class AccessDeniedController {
    private final CanonicalUrlFactory canonicalUrlFactory;

    @GetMapping("/403")
    public String accessDenied(Model model) {
        var seo =
                new SeoMetadata(
                        "Access denied",
                        "You do not have permission to access this page.",
                        "/403",
                        "noindex,nofollow");

        model.addAttribute("seo", seo);
        model.addAttribute("canonicalUrl", canonicalUrlFactory.forPath(seo.canonicalPath()));
        return "403";
    }
}
