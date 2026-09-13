package com.springbootecommerce.shophappens.administration.web.api;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.springbootecommerce.shophappens.catalog.application.port.in.AssignCatalogAttributeCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.CatalogAttributeAssignmentUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductNotFoundException;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.security.SecurityConfiguration;
import com.springbootecommerce.shophappens.security.service.CartMergingAuthenticationSuccessHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CatalogAttributeAssignmentAdminApiController.class)
@Import(SecurityConfiguration.class)
class CatalogAttributeAssignmentAdminApiControllerTest {
    private static final String BODY = "{\"definitionCode\":\"color\",\"value\":\"blue\"}";
    private static final ProductReference FAMILY = new ProductReference(11);
    private static final AssignCatalogAttributeCommand COMMAND =
            new AssignCatalogAttributeCommand("color", "blue");

    @Autowired MockMvc mvc;

    @MockitoBean CatalogAttributeAssignmentUseCase assignments;
    @MockitoBean CartMergingAuthenticationSuccessHandler successHandler;

    @Test
    void adminAssignsVariantAttributeWithinItsProductFamily() throws Exception {
        mvc.perform(
                        post("/api/admin/products/11/variants/202/attributes")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(BODY))
                .andExpect(status().isNoContent());

        verify(assignments).assignToVariant(FAMILY, 202, COMMAND);
    }

    @Test
    void missingParentReturnsCatalogNotFound() throws Exception {
        doThrow(new ProductNotFoundException(FAMILY))
                .when(assignments)
                .assignToVariant(FAMILY, 202, COMMAND);

        mvc.perform(
                        post("/api/admin/products/11/variants/202/attributes")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("catalog.product.not-found"));
    }

    @Test
    void customerCannotAssignVariantAttributes() throws Exception {
        mvc.perform(
                        post("/api/admin/products/11/variants/202/attributes")
                                .with(user("customer").roles("CUSTOMER"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(BODY))
                .andExpect(status().isForbidden());

        verifyNoInteractions(assignments);
    }

    @Test
    void csrfIsRequiredBeforeVariantAttributeAssignment() throws Exception {
        mvc.perform(
                        post("/api/admin/products/11/variants/202/attributes")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(BODY))
                .andExpect(status().isForbidden());

        verifyNoInteractions(assignments);
    }
}
