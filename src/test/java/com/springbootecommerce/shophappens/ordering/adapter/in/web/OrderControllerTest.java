package com.springbootecommerce.shophappens.ordering.adapter.in.web;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.springbootecommerce.shophappens.customer.application.port.in.CurrentCustomerIdentity;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderQuery;
import com.springbootecommerce.shophappens.security.SecurityConfiguration;
import com.springbootecommerce.shophappens.security.service.CartMergingAuthenticationSuccessHandler;
import com.springbootecommerce.shophappens.shared.web.CanonicalUrlFactory;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
@Import({CanonicalUrlFactory.class, SecurityConfiguration.class})
class OrderControllerTest {
    @Autowired MockMvc mvc;

    @MockitoBean OrderQuery orders;
    @MockitoBean CurrentCustomerIdentity currentCustomer;
    @MockitoBean CartMergingAuthenticationSuccessHandler successHandler;

    @BeforeEach
    void authenticatedCustomer() {
        when(currentCustomer.current()).thenReturn(Optional.of(new CustomerReference(42L)));
        when(orders.findAll(new CustomerId(42L))).thenReturn(List.of());
    }

    @Test
    void anonymousOrdersRedirectToLogin() throws Exception {
        mvc.perform(get("/orders"))
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .redirectedUrl("/login"));
    }

    @Test
    void authenticatedCustomerSeesOnlyOwnedOrderHistory() throws Exception {
        mvc.perform(get("/orders").with(user("customer").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(view().name("ordering/order-list"));
    }
}
