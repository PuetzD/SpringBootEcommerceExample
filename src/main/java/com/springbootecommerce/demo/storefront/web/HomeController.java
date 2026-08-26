package com.springbootecommerce.demo.storefront.web;

import com.springbootecommerce.demo.shared.web.CanonicalUrlFactory;
import com.springbootecommerce.demo.shared.web.SeoMetadata;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
  private static final String HOMEPAGE_TITLE = "Shop Happens - Buy stuff. Be happy.";
  private static final String HOMEPAGE_DESCRIPTION = "Buy stuff. Be happy.";

  private final CanonicalUrlFactory canonicalUrlFactory;

  public HomeController(CanonicalUrlFactory canonicalUrlFactory) {
    this.canonicalUrlFactory = canonicalUrlFactory;
  }

  @GetMapping("/")
  public String getHomepage(Model model, HttpServletRequest request) {
    var seo =
        new SeoMetadata(
            HOMEPAGE_TITLE,
            HOMEPAGE_DESCRIPTION,
            "/",
            "index,follow",
            HOMEPAGE_TITLE,
            HOMEPAGE_DESCRIPTION,
            "website");

    model.addAttribute("seo", seo);
    model.addAttribute("canonicalUrl", canonicalUrlFactory.forRequest(request, seo.canonicalPath()));
    return "storefront/homepage";
  }
}
