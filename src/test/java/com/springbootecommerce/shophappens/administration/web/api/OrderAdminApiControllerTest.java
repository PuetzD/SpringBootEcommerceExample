package com.springbootecommerce.shophappens.administration.web.api;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminDetail;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminMetrics;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminPage;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminSearch;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminSummary;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdministrationQuery;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderItemView;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderReference;
import com.springbootecommerce.shophappens.ordering.notification.application.port.in.OrderConfirmationAdministrationQuery;
import com.springbootecommerce.shophappens.ordering.notification.application.port.in.OrderConfirmationDeliveryView;
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
    @MockitoBean OrderConfirmationAdministrationQuery orderConfirmationAdministrationQuery;
    @MockitoBean CartMergingAuthenticationSuccessHandler successHandler;

    @Test
    void adminCanListOrdersByOrderNumber() throws Exception {
        var summary = summary("ORD-2026-100001");
        when(orderAdministrationQuery.searchOrders(new OrderAdminSearch(0, 20, "100001")))
                .thenReturn(
                        new OrderAdminPage(
                                List.of(summary),
                                0,
                                20,
                                1,
                                1,
                                new OrderAdminMetrics(Money.zero())));

        mockMvc.perform(
                        get("/api/admin/orders")
                                .param("q", "100001")
                                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(summary.orderNumber()))
                .andExpect(
                        jsonPath("$.content[0].orderId").value(summary.order().value().toString()))
                .andExpect(jsonPath("$.content[0].orderNumber").value(summary.orderNumber()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void adminCanListOrdersInAnInclusiveExclusivePeriodWithRevenueMetadata() throws Exception {
        Instant from = Instant.parse("2026-08-18T12:00:00Z");
        Instant to = Instant.parse("2026-09-17T12:00:00Z");
        var summary = summary("ORD-2026-100001");
        when(orderAdministrationQuery.searchOrders(new OrderAdminSearch(0, 20, null, from, to)))
                .thenReturn(
                        new OrderAdminPage(
                                List.of(summary),
                                0,
                                20,
                                2,
                                1,
                                new OrderAdminMetrics(new Money(new BigDecimal("136.96")))));

        mockMvc.perform(
                        get("/api/admin/orders")
                                .param("from", from.toString())
                                .param("to", to.toString())
                                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(summary.orderNumber()))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.meta.revenue").value(136.96))
                .andExpect(jsonPath("$.meta.currency").value("EUR"));
    }

    @Test
    void rejectsAnEmptyOrderPeriodBeforeQueryingOrders() throws Exception {
        Instant boundary = Instant.parse("2026-09-17T12:00:00Z");

        mockMvc.perform(
                        get("/api/admin/orders")
                                .param("from", boundary.toString())
                                .param("to", boundary.toString())
                                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("catalog.invalid"));
    }

    @Test
    void adminCanViewOrderDetails() throws Exception {
        var orderNumber = "ORD-2026-100002";
        when(orderAdministrationQuery.findOrder(orderNumber))
                .thenReturn(Optional.of(detail(orderNumber)));

        mockMvc.perform(
                        get("/api/admin/orders/{orderNumber}", orderNumber)
                                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderNumber))
                .andExpect(jsonPath("$.orderId").isNotEmpty())
                .andExpect(jsonPath("$.orderNumber").value(orderNumber))
                .andExpect(jsonPath("$.customerId").value(7))
                .andExpect(jsonPath("$.items[0].variantId").value(202))
                .andExpect(jsonPath("$.items[0].productId").value(7))
                .andExpect(jsonPath("$.items[0].unitPrice").value(19.99))
                .andExpect(jsonPath("$.items[0].currency").value("EUR"));
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
        when(orderAdministrationQuery.findOrder("ORD-2026-100003")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/admin/orders/ORD-2026-100003").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ordering.order.not-found"));
    }

    @Test
    void existingOrderReturnsConfirmationStatus() throws Exception {
        var orderNumber = "ORD-2026-100002";
        when(orderConfirmationAdministrationQuery.findForOrderNumber(orderNumber))
                .thenReturn(Optional.of(confirmationDetail(orderNumber)));
        mockMvc.perform(
                        get("/api/admin/orders/ORD-2026-100002/confirmation-delivery")
                                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("somestatus"))
                .andExpect(jsonPath("$.failedAttempts").value(2))
                .andExpect(jsonPath("$.lastError").value("someError"))
                .andExpect(jsonPath("$.retryAt").value("2026-09-05T09:00:00Z"));
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
                List.of(
                        new OrderItemView(
                                202L,
                                7L,
                                "SHIRT-L",
                                "T-Shirt",
                                new Money(new BigDecimal("19.99")),
                                1,
                                new Money(new BigDecimal("19.99")))),
                List.of());
    }

    private static OrderConfirmationDeliveryView confirmationDetail(String orderNumber) {
        return new OrderConfirmationDeliveryView(
                "somestatus",
                2,
                "someError",
                Instant.parse("2026-09-05T09:00:00Z"),
                Instant.parse("2026-09-05T09:00:00Z"));
    }
}
