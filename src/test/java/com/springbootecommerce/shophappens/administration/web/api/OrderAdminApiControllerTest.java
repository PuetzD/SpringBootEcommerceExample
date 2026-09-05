package com.springbootecommerce.shophappens.administration.web.api;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminDetail;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminPage;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminSearch;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminSummary;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdministrationQuery;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderReference;
import com.springbootecommerce.shophappens.security.SecurityConfiguration;
import com.springbootecommerce.shophappens.security.service.CartMergingAuthenticationSuccessHandler;
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

@WebMvcTest(OrderAdminApiController.class)
@Import(SecurityConfiguration.class)
class OrderAdminApiControllerTest {
    @Autowired MockMvc mockMvc;

    @MockitoBean OrderAdministrationQuery orderAdministrationQuery;
    @MockitoBean CartMergingAuthenticationSuccessHandler successHandler;

    @Test
    void adminCanListOrdersByOrderNumber() throws Exception {
        var summary = summary("ORD-20260905-ORDERADMIN1");
        when(orderAdministrationQuery.searchOrders(new OrderAdminSearch(0, 20, "ORDERADMIN")))
                .thenReturn(new OrderAdminPage(List.of(summary), 0, 20, 1, 1));

        mockMvc.perform(
                        get("/api/admin/orders")
                                .param("q", "ORDERADMIN")
                                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(summary.orderNumber()))
                .andExpect(jsonPath("$.content[0].orderNumber").value(summary.orderNumber()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void adminCanViewOrderDetails() throws Exception {
        var orderNumber = "ORD-20260905-ORDERDETAIL";
        when(orderAdministrationQuery.findOrder(orderNumber))
                .thenReturn(Optional.of(detail(orderNumber)));

        mockMvc.perform(
                        get("/api/admin/orders/{orderNumber}", orderNumber)
                                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderNumber))
                .andExpect(jsonPath("$.orderNumber").value(orderNumber))
                .andExpect(jsonPath("$.customerId").value(7));
    }

    @Test
    void rejectsOversizedOrderPages() throws Exception {
        mockMvc.perform(
                        get("/api/admin/orders")
                                .param("size", "101")
                                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void customerReceivesForbiddenForOrders() throws Exception {
        mockMvc.perform(get("/api/admin/orders").with(user("customer").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void missingOrderReturnsOrderingNotFoundError() throws Exception {
        when(orderAdministrationQuery.findOrder("ORD-20260905-MISSING1"))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        get("/api/admin/orders/ORD-20260905-MISSING1")
                                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ordering.order.not-found"));
    }

    private static OrderAdminSummary summary(String orderNumber) {
        return new OrderAdminSummary(
                new OrderReference(UUID.randomUUID()),
                orderNumber,
                new CustomerId(7),
                new Money(new BigDecimal("19.99")),
                Instant.parse("2026-09-05T09:00:00Z"));
    }

    private static OrderAdminDetail detail(String orderNumber) {
        return new OrderAdminDetail(
                new OrderReference(UUID.randomUUID()),
                orderNumber,
                new CustomerId(7),
                new Money(new BigDecimal("19.99")),
                Instant.parse("2026-09-05T09:00:00Z"),
                List.of(),
                List.of());
    }
}
