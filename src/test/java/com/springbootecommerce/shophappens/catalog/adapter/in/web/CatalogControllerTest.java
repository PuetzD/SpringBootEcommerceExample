package com.springbootecommerce.shophappens.catalog.adapter.in.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.springbootecommerce.shophappens.catalog.application.port.in.BrowseCatalogUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.BrowseCategoriesUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.CatalogPage;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategorySummary;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductSummary;
import com.springbootecommerce.shophappens.security.SecurityConfiguration;
import com.springbootecommerce.shophappens.security.service.CartMergingAuthenticationSuccessHandler;
import com.springbootecommerce.shophappens.shared.web.CanonicalUrlFactory;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CatalogController.class)
@Import({CanonicalUrlFactory.class, SecurityConfiguration.class})
class CatalogControllerTest {
    @Autowired MockMvc mockMvc;

    @MockitoBean BrowseCatalogUseCase catalog;

    @MockitoBean(name = "categoryQueryService")
    BrowseCategoriesUseCase categories;

    @MockitoBean CartMergingAuthenticationSuccessHandler successHandler;

    @Test
    void rendersCatalogAndProductDetail() throws Exception {
        var product = productSummary(7L, "WEAP-002", "Rubber Duck of Debugging", "18.99");
        when(categories.findAllActive()).thenReturn(List.of());
        when(catalog.findActivePage(0, 20))
                .thenReturn(new CatalogPage(List.of(product), 0, 20, 1, 1));
        when(catalog.findActiveBySku("WEAP-002")).thenReturn(Optional.of(product));
        when(catalog.findActiveVariants(product.product())).thenReturn(List.of(product));

        mockMvc.perform(get("/catalog"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/list"))
                .andExpect(model().attribute("products", List.of(product)));

        mockMvc.perform(get("/catalog/products/WEAP-002"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/detail"))
                .andExpect(model().attribute("product", product))
                .andExpect(model().attribute("variants", List.of(product)))
                .andExpect(content().string(not(containsString("id=\"variant-selector\""))));
    }

    @Test
    void delegatesRequestedCatalogPageAndExposesNavigationMetadata() throws Exception {
        var firstPageProduct = productSummary(7L, "PAGE-002", "Second page product", "18.99");
        when(catalog.findActivePage(1, 20))
                .thenReturn(new CatalogPage(List.of(firstPageProduct), 1, 20, 21, 2));

        mockMvc.perform(get("/catalog").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(
                        model().attribute(
                                        "catalogPage",
                                        new CatalogPage(List.of(firstPageProduct), 1, 20, 21, 2)))
                .andExpect(model().attribute("products", List.of(firstPageProduct)))
                .andExpect(content().string(containsString("Previous")))
                .andExpect(content().string(not(containsString(">Next<"))));

        verify(catalog).findActivePage(1, 20);
    }

    @Test
    void rejectsNegativeCatalogPage() throws Exception {
        mockMvc.perform(get("/catalog").param("page", "-1")).andExpect(status().isBadRequest());
    }

    @Test
    void selectsAnOwnedVariantAndRejectsForeignSelection() throws Exception {
        var family = productSummary(7L, 101L, "SHIRT-S", "Shirt", "10.00");
        var large = productSummary(7L, 202L, "SHIRT-L", "Shirt", "20.00");
        when(catalog.findActiveBySku("SHIRT-S")).thenReturn(Optional.of(family));
        when(catalog.findActiveVariants(family.product())).thenReturn(List.of(family, large));

        mockMvc.perform(get("/catalog/products/SHIRT-S").param("variant", "202"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("product", large))
                .andExpect(model().attribute("variants", List.of(family, large)))
                .andExpect(content().string(containsString("SHIRT-L")))
                .andExpect(content().string(containsString("20.00 EUR")))
                .andExpect(content().string(containsString("name=\"variant\" value=\"202\"")))
                .andExpect(
                        content()
                                .string(
                                        containsString(
                                                "rel=\"canonical\" href=\"http://localhost:8080/catalog/products/SHIRT-S\"")))
                .andExpect(
                        content()
                                .string(
                                        not(
                                                containsString(
                                                        "canonical\" href=\"http://localhost:8080/catalog/products/SHIRT-S?"))));

        mockMvc.perform(get("/catalog/products/SHIRT-S").param("variant", "303"))
                .andExpect(status().isNotFound());
    }

    @Test
    void omittedSelectionUsesTheActiveDefaultVariant() throws Exception {
        var family = productSummary(7L, 101L, "SHIRT-S", "Shirt", "10.00");
        var large = productSummary(7L, 202L, "SHIRT-L", "Shirt", "20.00");
        when(catalog.findActiveBySku("SHIRT-S")).thenReturn(Optional.of(family));
        when(catalog.findActiveVariants(family.product())).thenReturn(List.of(family, large));

        mockMvc.perform(get("/catalog/products/SHIRT-S"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("product", family));
    }

    @Test
    void omittedSelectionFallsBackToFirstActiveSiblingWhenDefaultIsWithdrawn() throws Exception {
        var withdrawnDefault = productSummary(7L, 101L, "SHIRT-S", "Shirt", "10.00");
        var activeSibling = productSummary(7L, 202L, "SHIRT-L", "Shirt", "20.00");
        when(catalog.findActiveBySku("SHIRT-S")).thenReturn(Optional.of(withdrawnDefault));
        when(catalog.findActiveVariants(withdrawnDefault.product()))
                .thenReturn(List.of(activeSibling));

        mockMvc.perform(get("/catalog/products/SHIRT-S"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("product", activeSibling))
                .andExpect(content().string(containsString("name=\"variant\" value=\"202\"")));
    }

    @Test
    void familyWithoutActiveVariantsCannotBeAddedToCart() throws Exception {
        var withdrawnDefault = productSummary(7L, 101L, "SHIRT-S", "Shirt", "10.00");
        when(catalog.findActiveBySku("SHIRT-S")).thenReturn(Optional.of(withdrawnDefault));
        when(catalog.findActiveVariants(withdrawnDefault.product())).thenReturn(List.of());

        mockMvc.perform(get("/catalog/products/SHIRT-S"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("variantAvailable", false))
                .andExpect(content().string(containsString("no available variants")))
                .andExpect(content().string(not(containsString("Add to cart"))));
    }

    @Test
    void returnsNotFoundForMissingOrInactiveProduct() throws Exception {
        when(catalog.findActiveBySku("MISSING")).thenReturn(Optional.empty());

        mockMvc.perform(get("/catalog/products/MISSING")).andExpect(status().isNotFound());
    }

    @Test
    void rendersProductsForCategorySlug() throws Exception {
        var category = new CategorySummary(new CategoryReference(1L), "Weapons", "weapons", 1);
        var product = productSummary(7L, "WEAP-002", "Rubber Duck of Debugging", "18.99");
        when(categories.findAllActive()).thenReturn(List.of(category));
        when(categories.findBySlug("weapons")).thenReturn(Optional.of(category));
        when(categories.findActiveProductsByCategorySlug("weapons")).thenReturn(List.of(product));

        mockMvc.perform(get("/catalog/categories/weapons"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/category"))
                .andExpect(model().attribute("category", category))
                .andExpect(model().attribute("products", List.of(product)));
    }

    @Test
    void returnsNotFoundForUnknownCategorySlug() throws Exception {
        when(categories.findBySlug("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/catalog/categories/unknown")).andExpect(status().isNotFound());
    }

    private ProductSummary productSummary(Long id, String sku, String name, String price) {
        return productSummary(id, id, sku, name, price);
    }

    private ProductSummary productSummary(
            Long id, Long variantId, String sku, String name, String price) {
        return new ProductSummary(
                new ProductReference(id),
                sku,
                name,
                "Description for " + name,
                new Money(new BigDecimal(price)),
                10,
                "/images/product-placeholder.svg",
                new ProductVariantId(variantId));
    }
}
