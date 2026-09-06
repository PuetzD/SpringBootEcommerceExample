package com.springbootecommerce.shophappens.account.adapter.in.web;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.springbootecommerce.shophappens.customer.application.port.in.CurrentCustomerIdentity;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.security.SecurityConfiguration;
import com.springbootecommerce.shophappens.security.service.CartMergingAuthenticationSuccessHandler;
import com.springbootecommerce.shophappens.shared.web.CanonicalUrlFactory;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AccountController.class)
@Import({CanonicalUrlFactory.class, SecurityConfiguration.class})
class AccountControllerTest {
    @Autowired MockMvc mvc;

    @MockitoBean CurrentCustomerIdentity currentCustomer;
    @MockitoBean CartMergingAuthenticationSuccessHandler successHandler;

    @Test
    void redirectsAnonymousUsersToLogin() throws Exception {
        mvc.perform(get("/account")).andExpect(status().is3xxRedirection());
    }

    @Test
    void rendersTheAccountOverviewForAnAuthenticatedCustomer() throws Exception {
        when(currentCustomer.current()).thenReturn(Optional.of(new CustomerReference(42L)));

        mvc.perform(get("/account").with(user("alex").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(view().name("account/overview"));
    }
}
