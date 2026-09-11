package com.springbootecommerce.shophappens.catalog.adapter.in.web;

import com.springbootecommerce.shophappens.catalog.application.port.in.BrowseCatalogUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.BrowseCategoriesUseCase;
import com.springbootecommerce.shophappens.shared.web.CanonicalUrlFactory;
import com.springbootecommerce.shophappens.shared.web.SeoMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CatalogController {
    private static final String LIST_TITLE = "Products";
    private static final String LIST_DESCRIPTION = "Browse the E-Shop catalog.";

    private final BrowseCatalogUseCase catalog;
    private final BrowseCategoriesUseCase categories;
    private final CanonicalUrlFactory canonicalUrlFactory;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must not be negative");
        }
        var catalogPage = catalog.findActivePage(page, 20);
        var seo = new SeoMetadata(LIST_TITLE, LIST_DESCRIPTION, "/catalog", "index,follow");
        model.addAttribute("seo", seo);
        model.addAttribute("canonicalUrl", canonicalUrlFactory.forPath(seo.canonicalPath()));
        model.addAttribute("catalogPage", catalogPage);
        model.addAttribute("products", catalogPage.products());
        return "catalog/list";
    }

    @GetMapping("/products/{sku}")
    public String detail(
            @PathVariable String sku, @RequestParam(required = false) Long variant, Model model) {
        var family =
                catalog.findActiveBySku(sku)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var variants = catalog.findActiveVariants(family.product());
        var product =
                variant == null
                        ? variants.stream()
                                .filter(candidate -> candidate.variant().equals(family.variant()))
                                .findFirst()
                                .or(() -> variants.stream().findFirst())
                                .orElse(family)
                        : variants.stream()
                                .filter(candidate -> candidate.variant().value() == variant)
                                .findFirst()
                                .orElseThrow(
                                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var path = "/catalog/products/" + sku;
        var seo = new SeoMetadata(product.name(), product.description(), path, "index,follow");
        model.addAttribute("seo", seo);
        model.addAttribute("canonicalUrl", canonicalUrlFactory.forPath(seo.canonicalPath()));
        model.addAttribute("product", product);
        model.addAttribute("variants", variants);
        model.addAttribute("variantAvailable", variants.contains(product));
        model.addAttribute("navigationSku", sku);
        return "catalog/detail";
    }

    @GetMapping("/categories/{slug}")
    public String category(@PathVariable String slug, Model model) {
        var category =
                categories
                        .findBySlug(slug)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var path = "/catalog/categories/" + slug;
        var seo =
                new SeoMetadata(
                        category.name(), "Browse " + category.name() + ".", path, "index,follow");
        model.addAttribute("seo", seo);
        model.addAttribute("canonicalUrl", canonicalUrlFactory.forPath(seo.canonicalPath()));
        model.addAttribute("category", category);
        model.addAttribute("products", categories.findActiveProductsByCategorySlug(slug));
        return "catalog/category";
    }
}
