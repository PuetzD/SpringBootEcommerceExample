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
public class AccessDeniedController {
  private final CanonicalUrlFactory canonicalUrlFactory;

  @GetMapping("/403")
  public String accessDenied(Model model, HttpServletRequest request) {
    var seo =
        new SeoMetadata(
            "Access denied",
            "You do not have permission to access this page.",
            "/403",
            "noindex,nofollow");

    model.addAttribute("seo", seo);
    model.addAttribute(
        "canonicalUrl", canonicalUrlFactory.forRequest(request, seo.canonicalPath()));
    return "403";
  }
}
