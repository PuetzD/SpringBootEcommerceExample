package com.springbootecommerce.shophappens.administration.web.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdminPage;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdminSearch;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdminView;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdministrationQuery;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdministrationUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryOption;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryRevision;
import com.springbootecommerce.shophappens.catalog.application.port.in.CreateCategoryCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.RenameCategoryCommand;
import com.springbootecommerce.shophappens.security.SecurityConfiguration;
import com.springbootecommerce.shophappens.security.service.CartMergingAuthenticationSuccessHandler;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CategoryAdminApiController.class)
@Import(SecurityConfiguration.class)
class CategoryAdminApiControllerTest {
    @Autowired MockMvc mockMvc;

    @MockitoBean CategoryAdministrationQuery categoryAdminQuery;
    @MockitoBean CategoryAdministrationUseCase categoryAdministrationUseCase;
    @MockitoBean CartMergingAuthenticationSuccessHandler successHandler;

    @Test
    void adminCanListCategories() throws Exception {
        when(categoryAdminQuery.listCategories(any(CategoryAdminSearch.class)))
                .thenReturn(
                        new CategoryAdminPage(
                                List.of(
                                        new CategoryAdminView(
                                                new CategoryReference(7L),
                                                "Tools",
                                                "tools",
                                                new CategoryRevision(0),
                                                3)),
                                0,
                                20,
                                1,
                                1));

        mockMvc.perform(get("/api/admin/categories").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(7))
                .andExpect(jsonPath("$.content[0].slug").value("tools"))
                .andExpect(jsonPath("$.content[0].revision").value(0))
                .andExpect(jsonPath("$.content[0].productCount").value(3))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(categoryAdminQuery).listCategories(new CategoryAdminSearch(0, 20));
    }

    @Test
    void adminCanLoadCategoryDetail() throws Exception {
        when(categoryAdminQuery.findCategory(new CategoryReference(7L)))
                .thenReturn(
                        Optional.of(
                                new CategoryAdminView(
                                        new CategoryReference(7L),
                                        "Tools",
                                        "tools",
                                        new CategoryRevision(2),
                                        3)));

        mockMvc.perform(get("/api/admin/categories/7").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Tools"))
                .andExpect(jsonPath("$.slug").value("tools"))
                .andExpect(jsonPath("$.revision").value(2))
                .andExpect(jsonPath("$.productCount").value(3));
    }

    @Test
    void adminCanLoadCategoryOptions() throws Exception {
        when(categoryAdminQuery.listCategoryOptions())
                .thenReturn(
                        List.of(new CategoryOption(new CategoryReference(7L), "Tools", "tools")));

        mockMvc.perform(get("/api/admin/categories/options").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].name").value("Tools"))
                .andExpect(jsonPath("$[0].slug").value("tools"));
    }

    @Test
    void rejectsOversizedCategoryPages() throws Exception {
        mockMvc.perform(
                        get("/api/admin/categories")
                                .param("size", "101")
                                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void customerReceivesForbiddenForCategories() throws Exception {
        mockMvc.perform(get("/api/admin/categories").with(user("customer").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void missingCategoryReturnsCatalogNotFoundError() throws Exception {
        when(categoryAdminQuery.findCategory(new CategoryReference(7L)))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/admin/categories/7").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("catalog.category.not-found"));
    }

    @Test
    void invalidCategoryPayloadReturnsBadRequest() throws Exception {
        mockMvc.perform(
                        post("/api/admin/categories")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCategoryReturnsCreatedLocation() throws Exception {
        when(categoryAdministrationUseCase.createCategory(any(CreateCategoryCommand.class)))
                .thenReturn(
                        new CategoryAdminView(
                                new CategoryReference(7L),
                                "Tools",
                                "tools",
                                new CategoryRevision(0),
                                0));

        mockMvc.perform(
                        post("/api/admin/categories")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"Tools\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/admin/categories/7"))
                .andExpect(jsonPath("$.name").value("Tools"))
                .andExpect(jsonPath("$.revision").value(0));

        verify(categoryAdministrationUseCase).createCategory(new CreateCategoryCommand("Tools"));
    }

    @Test
    void updateCategoryReturnsUpdatedCategory() throws Exception {
        when(categoryAdministrationUseCase.renameCategory(
                        eq(new CategoryReference(7L)),
                        eq(new CategoryRevision(0)),
                        any(RenameCategoryCommand.class)))
                .thenReturn(
                        new CategoryAdminView(
                                new CategoryReference(7L),
                                "Updated tools",
                                "updated-tools",
                                new CategoryRevision(1),
                                1));

        mockMvc.perform(
                        put("/api/admin/categories/7")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf())
                                .header("If-Match", "\"0\"")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"Updated tools\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated tools"))
                .andExpect(jsonPath("$.revision").value(1));

        verify(categoryAdministrationUseCase)
                .renameCategory(
                        new CategoryReference(7L),
                        new CategoryRevision(0),
                        new RenameCategoryCommand("Updated tools"));
    }

    @Test
    void deleteCategoryReturnsNoContent() throws Exception {
        mockMvc.perform(
                        delete("/api/admin/categories/7")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf())
                                .header("If-Match", "\"0\""))
                .andExpect(status().isNoContent());

        verify(categoryAdministrationUseCase)
                .deleteCategory(new CategoryReference(7L), new CategoryRevision(0));
    }
}
