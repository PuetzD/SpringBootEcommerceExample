package com.springbootecommerce.shophappens.catalog.web;

import com.springbootecommerce.shophappens.catalog.application.CatalogQueryService;
import com.springbootecommerce.shophappens.storefront.domain.CanonicalUrlFactory;
import com.springbootecommerce.shophappens.storefront.domain.SeoMetadata;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/catalog")
public class CatalogController {
    private static final String LIST_TITLE = "Products";
    private static final String LIST_DESCRIPTION = "Browse the E-Shop catalog.";

    private final CatalogQueryService catalog;
    private final CanonicalUrlFactory canonicalUrlFactory;

    public CatalogController(CatalogQueryService catalog, CanonicalUrlFactory canonicalUrlFactory) {
        this.catalog = catalog;
        this.canonicalUrlFactory = canonicalUrlFactory;
    }

    @GetMapping
    public String list(Model model) {
        var seo = new SeoMetadata(LIST_TITLE, LIST_DESCRIPTION, "/catalog", "index,follow");
        model.addAttribute("seo", seo);
        model.addAttribute("canonicalUrl", canonicalUrlFactory.forPath(seo.canonicalPath()));
        model.addAttribute("products", catalog.findAllActiveProducts());
        return "catalog/list";
    }

    @GetMapping("/products/{sku}")
    public String detail(@PathVariable String sku, Model model) {
        var product =
                catalog.findActiveProductBySku(sku)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var path = "/catalog/products/" + sku;
        var seo = new SeoMetadata(product.name(), product.description(), path, "index,follow");
        model.addAttribute("seo", seo);
        model.addAttribute("canonicalUrl", canonicalUrlFactory.forPath(seo.canonicalPath()));
        model.addAttribute("product", product);
        return "catalog/detail";
    }
}
