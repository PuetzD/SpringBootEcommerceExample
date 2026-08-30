package com.springbootecommerce.shophappens.administration.web.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.springbootecommerce.shophappens.security.SecurityConfiguration;
import com.springbootecommerce.shophappens.security.service.CartMergingAuthenticationSuccessHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminCsrfController.class)
@Import({SecurityConfiguration.class})
public class AdminCsrfControllerTest {
    @Autowired MockMvc mockMvc;

    @MockitoBean CartMergingAuthenticationSuccessHandler successHandler;

    @Test
    void adminReceivesCsrfToken() throws Exception {
        mockMvc.perform(get("/api/admin/csrf")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").isString())
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    void customerReceivesForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/csrf")
                        .with(user("customer").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedReceivesRedirect() throws Exception {
        mockMvc.perform(get("/api/admin/csrf"))
                .andExpect(status().is3xxRedirection());
    }
}
