package com.springbootecommerce.shophappens.catalog.adapter.in.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.springbootecommerce.shophappens.catalog.application.port.in.BrowseCatalogUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductSummary;
import com.springbootecommerce.shophappens.security.SecurityConfiguration;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import com.springbootecommerce.shophappens.web.support.CanonicalUrlFactory;
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

    @Test
    void rendersCatalogAndProductDetail() throws Exception {
        var product = productSummary(7L, "ELEC-001", "Headphones", "149.99");
        when(catalog.findAllActive()).thenReturn(List.of(product));
        when(catalog.findActiveBySku("ELEC-001")).thenReturn(Optional.of(product));

        mockMvc.perform(get("/catalog"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/list"))
                .andExpect(model().attribute("products", List.of(product)));

        mockMvc.perform(get("/catalog/products/ELEC-001"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/detail"))
                .andExpect(model().attribute("product", product));
    }

    @Test
    void returnsNotFoundForMissingOrInactiveProduct() throws Exception {
        when(catalog.findActiveBySku("MISSING")).thenReturn(Optional.empty());

        mockMvc.perform(get("/catalog/products/MISSING")).andExpect(status().isNotFound());
    }

    private ProductSummary productSummary(Long id, String sku, String name, String price) {
        return new ProductSummary(
                new ProductReference(id),
                sku,
                name,
                "Description for " + name,
                new Money(new BigDecimal(price)),
                10,
                "/images/product-placeholder.svg");
    }
}
