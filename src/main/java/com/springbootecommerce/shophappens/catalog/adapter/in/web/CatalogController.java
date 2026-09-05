package com.springbootecommerce.shophappens.catalog.adapter.in.web;

import com.springbootecommerce.shophappens.catalog.application.port.in.BrowseCatalogUseCase;
import com.springbootecommerce.shophappens.shared.web.CanonicalUrlFactory;
import com.springbootecommerce.shophappens.shared.web.SeoMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CatalogController {
    private static final String LIST_TITLE = "Products";
    private static final String LIST_DESCRIPTION = "Browse the E-Shop catalog.";

    private final BrowseCatalogUseCase catalog;
    private final CanonicalUrlFactory canonicalUrlFactory;

    @GetMapping
    public String list(Model model) {
        var seo = new SeoMetadata(LIST_TITLE, LIST_DESCRIPTION, "/catalog", "index,follow");
        model.addAttribute("seo", seo);
        model.addAttribute("canonicalUrl", canonicalUrlFactory.forPath(seo.canonicalPath()));
        model.addAttribute("products", catalog.findActivePage(0, 20).products());
        return "catalog/list";
    }

    @GetMapping("/products/{sku}")
    public String detail(@PathVariable String sku, Model model) {
        var product =
                catalog.findActiveBySku(sku)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var path = "/catalog/products/" + sku;
        var seo = new SeoMetadata(product.name(), product.description(), path, "index,follow");
        model.addAttribute("seo", seo);
        model.addAttribute("canonicalUrl", canonicalUrlFactory.forPath(seo.canonicalPath()));
        model.addAttribute("product", product);
        return "catalog/detail";
    }
}
