package com.springbootecommerce.shophappens.storefront.web;

import com.springbootecommerce.shophappens.catalog.application.port.in.BrowseCatalogUseCase;
import com.springbootecommerce.shophappens.web.support.CanonicalUrlFactory;
import com.springbootecommerce.shophappens.web.support.SeoMetadata;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private static final String HOMEPAGE_TITLE = "Shop Happens - Buy stuff. Be happy.";
    private static final String HOMEPAGE_DESCRIPTION = "Buy stuff. Be happy.";

    private final CanonicalUrlFactory canonicalUrlFactory;
    private final BrowseCatalogUseCase catalog;

    public HomeController(CanonicalUrlFactory canonicalUrlFactory, BrowseCatalogUseCase catalog) {
        this.canonicalUrlFactory = canonicalUrlFactory;
        this.catalog = catalog;
    }

    @GetMapping("/")
    public String getHomepage(Model model) {
        var seo = new SeoMetadata(HOMEPAGE_TITLE, HOMEPAGE_DESCRIPTION, "/", "index,follow");

        model.addAttribute("seo", seo);
        model.addAttribute("canonicalUrl", canonicalUrlFactory.forPath(seo.canonicalPath()));
        model.addAttribute(
                "featuredProducts",
                catalog.findAllActive().stream().limit(3).toList());
        return "storefront/homepage";
    }
}
