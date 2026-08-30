package com.springbootecommerce.shophappens.administration.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdministrationUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdministrationUseCase;
import com.springbootecommerce.shophappens.security.SecurityConfiguration;
import com.springbootecommerce.shophappens.security.service.CartMergingAuthenticationSuccessHandler;
import com.springbootecommerce.shophappens.shared.web.CanonicalUrlFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminController.class)
@Import({CanonicalUrlFactory.class, SecurityConfiguration.class})
class AdminControllerTest {
    @Autowired MockMvc mockMvc;

    @MockitoBean CartMergingAuthenticationSuccessHandler successHandler;
    @MockitoBean ProductAdministrationUseCase adminProduct;
    @MockitoBean CategoryAdministrationUseCase adminCategory;

    @Test
    void adminReceivesSpaForRoot() throws Exception {
        mockMvc.perform(get("/admin").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/admin/index.html"));
    }

    @Test
    void adminReceivesSpaForProducts() throws Exception {
        mockMvc.perform(get("/admin/products").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/admin/index.html"));
    }

    @Test
    void customerReceivesForbidden() throws Exception {
        mockMvc.perform(get("/admin").with(user("customer").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginPageRemainsReachable() throws Exception {
        mockMvc.perform(get("/admin/login")).andExpect(status().isOk());
    }

    @Test
    void apiRoutesNotHandledByFallback() throws Exception {
        mockMvc.perform(get("/api/admin/products")).andExpect(status().is3xxRedirection());
    }

    @Test
    void fallbackRejectsPathsWithFileExtension() throws Exception {
        mockMvc.perform(get("/admin/missing-font.woff").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }
}
