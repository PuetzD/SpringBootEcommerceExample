package com.springbootecommerce.shophappens.administration.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.springbootecommerce.shophappens.security.SecurityConfiguration;
import com.springbootecommerce.shophappens.security.service.CartMergingAuthenticationSuccessHandler;
import com.springbootecommerce.shophappens.security.web.AdminLoginController;
import com.springbootecommerce.shophappens.shared.web.CanonicalUrlFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({AdminController.class, AdminLoginController.class})
@Import({CanonicalUrlFactory.class, SecurityConfiguration.class})
class AdminControllerTest {
    @Autowired MockMvc mockMvc;

    @MockitoBean CartMergingAuthenticationSuccessHandler successHandler;

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/admin",
                "/admin/",
                "/admin/products",
                "/admin/products/create",
                "/admin/products/7",
                "/admin/categories",
                "/admin/categories/create",
                "/admin/categories/7",
                "/admin/orders",
                "/admin/orders/ORD-2026-100001/show",
                "/admin/customers",
                "/admin/customers/12/show",
                "/admin/storefront"
            })
    void adminReceivesSpaForEveryMountedRoute(String route) throws Exception {
        mockMvc.perform(get(route).with(user("admin").roles("ADMIN")))
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
    void anonymousApiRoutesReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/products")).andExpect(status().isUnauthorized());
    }

    @Test
    void nonAdminApiRoutesReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/products").with(user("customer").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/admin/dashboard",
                "/admin/unknown",
                "/admin/missing-font.woff",
                "/admin/products/7/show",
                "/admin/orders/create",
                "/admin/orders/invoice.pdf/show"
            })
    void rejectsUnknownOrExtensionLikePaths(String route) throws Exception {
        mockMvc.perform(get(route).with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }
}
