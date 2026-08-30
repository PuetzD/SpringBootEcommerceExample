package com.springbootecommerce.shophappens.account.adapter.in.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.springbootecommerce.shophappens.account.application.port.in.RegisterCustomerAccountUseCase;
import com.springbootecommerce.shophappens.security.SecurityConfiguration;
import com.springbootecommerce.shophappens.security.service.CartMergingAuthenticationSuccessHandler;
import com.springbootecommerce.shophappens.shared.web.CanonicalUrlFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RegistrationController.class)
@Import({CanonicalUrlFactory.class, SecurityConfiguration.class})
class RegistrationControllerTest {
    @Autowired MockMvc mvc;

    @MockitoBean RegisterCustomerAccountUseCase registration;
    @MockitoBean CartMergingAuthenticationSuccessHandler successHandler;

    @Test
    void rendersRegistrationAndSubmitsValidForm() throws Exception {
        mvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/register"));

        mvc.perform(
                        post("/register")
                                .with(csrf())
                                .param("email", "customer@example.com")
                                .param("password", "twelve-chars!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));
    }
}
