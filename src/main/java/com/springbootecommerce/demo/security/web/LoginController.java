package com.springbootecommerce.demo.security.web;

import com.springbootecommerce.demo.storefront.domain.CanonicalUrlFactory;
import com.springbootecommerce.demo.storefront.domain.SeoMetadata;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class LoginController {
  private final CanonicalUrlFactory canonicalUrlFactory;

  @GetMapping("/login")
  public String login(Model model, HttpServletRequest request) {
    var seo =
        new SeoMetadata("Sign in", "Sign in to your E-Shop account.", "/login", "noindex,follow");

    model.addAttribute("seo", seo);
    model.addAttribute(
        "canonicalUrl", canonicalUrlFactory.forRequest(request, seo.canonicalPath()));
    return "login";
  }
}
