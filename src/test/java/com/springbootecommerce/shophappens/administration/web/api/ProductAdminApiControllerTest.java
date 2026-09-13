package com.springbootecommerce.shophappens.administration.web.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.springbootecommerce.shophappens.catalog.application.port.in.AmbiguousProductUpdateException;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.CreateProductCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.DuplicateSkuException;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdminPage;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdminSearch;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdminView;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdministrationQuery;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdministrationUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductCategorySummary;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductRevision;
import com.springbootecommerce.shophappens.catalog.application.port.in.StaleProductRevisionException;
import com.springbootecommerce.shophappens.catalog.application.port.in.UpdateProductCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.UpdateProductFamilyCommand;
import com.springbootecommerce.shophappens.security.SecurityConfiguration;
import com.springbootecommerce.shophappens.security.service.CartMergingAuthenticationSuccessHandler;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductAdminApiController.class)
@Import(SecurityConfiguration.class)
class ProductAdminApiControllerTest {
    @Autowired MockMvc mockMvc;

    @MockitoBean ProductAdministrationQuery productAdminQuery;
    @MockitoBean ProductAdministrationUseCase productAdministrationUseCase;
    @MockitoBean CartMergingAuthenticationSuccessHandler successHandler;

    @Test
    void adminCanListProducts() throws Exception {
        ProductAdminView product =
                new ProductAdminView(
                        new ProductReference(1L),
                        "SKU-1",
                        "Widget",
                        "Useful widget",
                        new Money(BigDecimal.valueOf(19.99)),
                        7,
                        "https://example.com/widget.png",
                        true,
                        new ProductRevision(0),
                        List.of(
                                new ProductCategorySummary(
                                        new com.springbootecommerce.shophappens.catalog.application
                                                .port.in.CategoryReference(10L),
                                        "Tools",
                                        "tools")));
        when(productAdminQuery.searchProducts(any(ProductAdminSearch.class)))
                .thenReturn(new ProductAdminPage(List.of(product), 0, 20, 1, 1));

        mockMvc.perform(get("/api/admin/products").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].sku").value("SKU-1"))
                .andExpect(jsonPath("$.content[0].revision").value(0))
                .andExpect(jsonPath("$.content[0].categories[0].id").value(10))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
        verify(productAdminQuery).searchProducts(new ProductAdminSearch(0, 20, null, null));
    }

    @Test
    void forwardsDatabaseBoundedProductSearchParameters() throws Exception {
        when(productAdminQuery.searchProducts(new ProductAdminSearch(2, 25, "widget", true)))
                .thenReturn(new ProductAdminPage(List.of(), 2, 25, 0, 0));

        mockMvc.perform(
                        get("/api/admin/products")
                                .param("page", "2")
                                .param("size", "25")
                                .param("q", "widget")
                                .param("active", "true")
                                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(25));

        verify(productAdminQuery).searchProducts(new ProductAdminSearch(2, 25, "widget", true));
    }

    @Test
    void adminCanGetProductDetail() throws Exception {
        when(productAdminQuery.findProduct(new ProductReference(1L)))
                .thenReturn(
                        Optional.of(
                                new ProductAdminView(
                                        new ProductReference(1L),
                                        "SKU-1",
                                        "Widget",
                                        "Useful widget",
                                        new Money(BigDecimal.valueOf(19.99)),
                                        7,
                                        null,
                                        true,
                                        new ProductRevision(3),
                                        List.of(
                                                new ProductCategorySummary(
                                                        new CategoryReference(10L),
                                                        "Tools",
                                                        "tools")))));

        mockMvc.perform(get("/api/admin/products/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.revision").value(3))
                .andExpect(jsonPath("$.categories[0].id").value(10));
    }

    @Test
    void rejectsProductPagesOutsideDatabaseBounds() throws Exception {
        mockMvc.perform(
                        get("/api/admin/products")
                                .param("page", "-1")
                                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("catalog.invalid"));
        mockMvc.perform(
                        get("/api/admin/products")
                                .param("size", "0")
                                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("catalog.invalid"));
        mockMvc.perform(
                        get("/api/admin/products")
                                .param("size", "101")
                                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("catalog.invalid"));
    }

    @Test
    void customerReceivesForbiddenForProducts() throws Exception {
        mockMvc.perform(get("/api/admin/products").with(user("customer").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousUserReceivesUnauthorizedForProducts() throws Exception {
        mockMvc.perform(get("/api/admin/products")).andExpect(status().isUnauthorized());
    }

    @Test
    void missingProductReturnsCatalogNotFoundError() throws Exception {
        when(productAdminQuery.findProduct(new ProductReference(1L))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/admin/products/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("catalog.product.not-found"));
    }

    @Test
    void invalidProductPayloadReturnsBadRequest() throws Exception {
        mockMvc.perform(
                        post("/api/admin/products")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"sku\":\"\",\"name\":\"\",\"price\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("request.validation"))
                .andExpect(jsonPath("$.fieldErrors.sku").value("SKU is required"));
    }

    @Test
    void createProductReturnsCreatedLocation() throws Exception {
        when(productAdministrationUseCase.createProduct(any(CreateProductCommand.class)))
                .thenReturn(
                        new ProductAdminView(
                                new ProductReference(1L),
                                "SKU-1",
                                "Widget",
                                "Useful widget",
                                new Money(BigDecimal.valueOf(19.99)),
                                7,
                                "https://example.com/widget.png",
                                true,
                                new ProductRevision(0),
                                List.of()));

        mockMvc.perform(
                        post("/api/admin/products")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"sku\":\"SKU-1\",\"name\":\"Widget\",\"description\":\"Useful widget\",\"price\":19.99,\"stockQuantity\":7,\"imageUrl\":\"https://example.com/widget.png\",\"categoryIds\":[10]}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/admin/products/1"))
                .andExpect(jsonPath("$.sku").value("SKU-1"));
        verify(productAdministrationUseCase)
                .createProduct(
                        new CreateProductCommand(
                                "SKU-1",
                                "Widget",
                                "Useful widget",
                                new Money(BigDecimal.valueOf(19.99)),
                                7,
                                "https://example.com/widget.png",
                                Set.of(new CategoryReference(10L))));
    }

    @Test
    void updateProductReturnsUpdatedProduct() throws Exception {
        when(productAdministrationUseCase.updateProduct(
                        eq(new ProductReference(1L)),
                        any(ProductRevision.class),
                        any(UpdateProductCommand.class)))
                .thenReturn(
                        new ProductAdminView(
                                new ProductReference(1L),
                                "SKU-1",
                                "Updated widget",
                                "Updated description",
                                new Money(BigDecimal.valueOf(29.99)),
                                10,
                                "https://example.com/updated.png",
                                true,
                                new ProductRevision(1),
                                List.of()));

        mockMvc.perform(
                        put("/api/admin/products/1")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"revision\":0,\"name\":\"Updated widget\",\"description\":\"Updated description\",\"price\":29.99,\"stockQuantity\":10,\"imageUrl\":\"https://example.com/updated.png\",\"active\":true,\"categoryIds\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated widget"))
                .andExpect(jsonPath("$.revision").value(1));
        verify(productAdministrationUseCase)
                .updateProduct(
                        new ProductReference(1L),
                        new ProductRevision(0),
                        new UpdateProductCommand(
                                "Updated widget",
                                "Updated description",
                                new Money(BigDecimal.valueOf(29.99)),
                                10,
                                "https://example.com/updated.png",
                                true,
                                Set.of()));
    }

    @Test
    void editsFamilyWithoutCommercialFields() throws Exception {
        when(productAdministrationUseCase.updateProductFamily(any(), any(), any()))
                .thenReturn(
                        new ProductAdminView(
                                new ProductReference(11),
                                "TEE-A",
                                "Tee",
                                "Cotton",
                                new Money(new BigDecimal("10.00")),
                                5,
                                null,
                                true,
                                new ProductRevision(4),
                                List.of()));

        mockMvc.perform(
                        patch("/api/admin/products/11/family")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"revision\":3,\"name\":\"Tee\",\"description\":\"Cotton\",\"active\":true,\"categoryIds\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value(4));
        verify(productAdministrationUseCase)
                .updateProductFamily(
                        new ProductReference(11),
                        new ProductRevision(3),
                        new UpdateProductFamilyCommand("Tee", "Cotton", true, Set.of()));
    }

    @Test
    void familyEditRequiresAdminAndCsrf() throws Exception {
        String body =
                "{\"revision\":3,\"name\":\"Tee\",\"description\":\"Cotton\",\"active\":true,\"categoryIds\":[]}";

        mockMvc.perform(
                        patch("/api/admin/products/11/family")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(
                        patch("/api/admin/products/11/family")
                                .with(user("customer").roles("CUSTOMER"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isForbidden());
        verifyNoInteractions(productAdministrationUseCase);
    }

    @Test
    void legacyCommercialEditRejectsVariantFamilies() throws Exception {
        when(productAdministrationUseCase.updateProduct(any(), any(), any()))
                .thenThrow(new AmbiguousProductUpdateException());

        mockMvc.perform(
                        put("/api/admin/products/1")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"revision\":0,\"name\":\"Updated widget\",\"description\":\"Updated description\",\"price\":29.99,\"stockQuantity\":10,\"imageUrl\":null,\"active\":true,\"categoryIds\":[]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("catalog.variant.required"));
    }

    @Test
    void deleteProductReturnsNoContent() throws Exception {
        mockMvc.perform(
                        delete("/api/admin/products/1")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf())
                                .header("If-Match", "\"4\""))
                .andExpect(status().isNoContent());
        verify(productAdministrationUseCase)
                .deactivateProduct(new ProductReference(1L), new ProductRevision(4L));
    }

    @Test
    void productDeletionRequiresQuotedRevisionAndCsrf() throws Exception {
        mockMvc.perform(
                        delete("/api/admin/products/1")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("catalog.invalid"));
        mockMvc.perform(
                        delete("/api/admin/products/1")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf())
                                .header("If-Match", "4"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("catalog.invalid"));
        mockMvc.perform(
                        delete("/api/admin/products/1")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf())
                                .header("If-Match", "\"4\"", "\"5\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("catalog.invalid"));
        mockMvc.perform(
                        delete("/api/admin/products/1")
                                .with(user("admin").roles("ADMIN"))
                                .header("If-Match", "\"4\""))
                .andExpect(status().isForbidden());
        verifyNoInteractions(productAdministrationUseCase);
    }

    @Test
    void duplicateSkuAndStaleRevisionUseStableConflictCodes() throws Exception {
        when(productAdministrationUseCase.createProduct(any(CreateProductCommand.class)))
                .thenThrow(new DuplicateSkuException("SKU-1"));
        mockMvc.perform(
                        post("/api/admin/products")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"sku\":\"SKU-1\",\"name\":\"Widget\",\"price\":19.99,\"stockQuantity\":7}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("catalog.product.sku-conflict"));

        when(productAdministrationUseCase.updateProduct(any(), any(), any()))
                .thenThrow(
                        new StaleProductRevisionException(
                                new ProductReference(1), new ProductRevision(0)));
        mockMvc.perform(
                        put("/api/admin/products/1")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"revision\":0,\"name\":\"Widget\",\"price\":19.99,\"stockQuantity\":7,\"active\":true}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("catalog.product.stale"));
    }
}
