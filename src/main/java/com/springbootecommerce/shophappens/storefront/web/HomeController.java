package com.springbootecommerce.shophappens.storefront.web;

import com.springbootecommerce.shophappens.catalog.application.CatalogQueryService;
import com.springbootecommerce.shophappens.catalog.web.CanonicalUrlFactory;
import com.springbootecommerce.shophappens.catalog.web.SeoMetadata;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private static final String HOMEPAGE_TITLE = "Shop Happens - Buy stuff. Be happy.";
    private static final String HOMEPAGE_DESCRIPTION = "Buy stuff. Be happy.";

    private final CanonicalUrlFactory canonicalUrlFactory;
    private final CatalogQueryService catalogQueryService;

    public HomeController(
            CanonicalUrlFactory canonicalUrlFactory, CatalogQueryService catalogQueryService) {
        this.canonicalUrlFactory = canonicalUrlFactory;
        this.catalogQueryService = catalogQueryService;
    }

    @GetMapping("/")
    public String getHomepage(Model model) {
        var seo = new SeoMetadata(HOMEPAGE_TITLE, HOMEPAGE_DESCRIPTION, "/", "index,follow");

        model.addAttribute("seo", seo);
        model.addAttribute("canonicalUrl", canonicalUrlFactory.forPath(seo.canonicalPath()));
        model.addAttribute(
                "featuredProducts",
                catalogQueryService.findAllActiveProducts().stream().limit(3).toList());
        return "storefront/homepage";
    }
}
