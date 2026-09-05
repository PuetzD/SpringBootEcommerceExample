package com.springbootecommerce.shophappens.administration.web.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.springbootecommerce.shophappens.customer.application.port.in.AddressReference;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdminAddressView;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdminDetail;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdminPage;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdminSearch;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdminSummary;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdministrationQuery;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminSummary;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdministrationQuery;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderReference;
import com.springbootecommerce.shophappens.security.SecurityConfiguration;
import com.springbootecommerce.shophappens.security.service.CartMergingAuthenticationSuccessHandler;
import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CustomerAdminApiController.class)
@Import(SecurityConfiguration.class)
class CustomerAdminApiControllerTest {
    @Autowired MockMvc mockMvc;

    @MockitoBean CustomerAdministrationQuery customerAdministrationQuery;
    @MockitoBean OrderAdministrationQuery orderAdministrationQuery;
    @MockitoBean CartMergingAuthenticationSuccessHandler successHandler;

    @Test
    void adminCanSearchCustomersAndUsesCustomerIdAsResponseId() throws Exception {
        var summary =
                new CustomerAdminSummary(new CustomerId(7), "Alice", "Admin", "alice@example.com");
        when(customerAdministrationQuery.searchCustomers(new CustomerAdminSearch(0, 20, "alice")))
                .thenReturn(new CustomerAdminPage(List.of(summary), 0, 20, 1, 1));

        mockMvc.perform(
                        get("/api/admin/customers")
                                .param("q", "alice")
                                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(7))
                .andExpect(jsonPath("$.content[0].givenName").value("Alice"))
                .andExpect(jsonPath("$.content[0].contactEmail").value("alice@example.com"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void forwardsRequestedPageAndSizeToCustomerQuery() throws Exception {
        when(customerAdministrationQuery.searchCustomers(any(CustomerAdminSearch.class)))
                .thenReturn(new CustomerAdminPage(List.of(), 2, 5, 0, 0));

        mockMvc.perform(
                        get("/api/admin/customers")
                                .param("page", "2")
                                .param("size", "5")
                                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        verify(customerAdministrationQuery).searchCustomers(new CustomerAdminSearch(2, 5, null));
    }

    @Test
    void adminCanViewCustomerAddressesAndLinkedOrders() throws Exception {
        long customerId = 7;
        var orderId = UUID.randomUUID();
        when(customerAdministrationQuery.findCustomer(new CustomerId(customerId)))
                .thenReturn(Optional.of(detail(customerId)));
        when(orderAdministrationQuery.findOrdersForCustomer(new CustomerId(customerId)))
                .thenReturn(
                        List.of(
                                new OrderAdminSummary(
                                        new OrderReference(orderId),
                                        "ORD-1001",
                                        new CustomerId(customerId),
                                        new Money(new BigDecimal("19.99")),
                                        Instant.parse("2026-09-05T09:00:00Z"))));

        mockMvc.perform(
                        get("/api/admin/customers/{customerId}", customerId)
                                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId))
                .andExpect(jsonPath("$.accountId").value(11))
                .andExpect(jsonPath("$.addresses[0].id").value(31))
                .andExpect(jsonPath("$.orders[0].orderNumber").value("ORD-1001"))
                .andExpect(jsonPath("$.orders[0].orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.orders[0].orderUrl").value("/admin/orders/ORD-1001"));
    }

    @Test
    void rejectsInvalidCustomerPages() throws Exception {
        mockMvc.perform(
                        get("/api/admin/customers")
                                .param("page", "-1")
                                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        get("/api/admin/customers")
                                .param("size", "101")
                                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsEmptyCustomerPage() throws Exception {
        when(customerAdministrationQuery.searchCustomers(any(CustomerAdminSearch.class)))
                .thenReturn(new CustomerAdminPage(List.of(), 0, 20, 0, 0));

        mockMvc.perform(get("/api/admin/customers").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void customerRoleCannotAccessCustomerAdministration() throws Exception {
        mockMvc.perform(get("/api/admin/customers").with(user("customer").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void missingCustomerReturnsCustomerNotFoundError() throws Exception {
        when(customerAdministrationQuery.findCustomer(new CustomerId(404)))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        get("/api/admin/customers/{customerId}", 404)
                                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("customer.not-found"));
    }

    private static CustomerAdminDetail detail(long customerId) {
        return new CustomerAdminDetail(
                new CustomerId(customerId),
                new AccountId(11),
                "Alice",
                "Admin",
                "alice@example.com",
                List.of(
                        new CustomerAdminAddressView(
                                new AddressReference(31),
                                "Alice Admin",
                                null,
                                "Main Street 1",
                                null,
                                "Berlin",
                                null,
                                "10115",
                                "DE",
                                null,
                                true,
                                false)));
    }
}
