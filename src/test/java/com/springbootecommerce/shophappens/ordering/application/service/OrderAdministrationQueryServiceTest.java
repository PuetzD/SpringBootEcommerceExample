package com.springbootecommerce.shophappens.ordering.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminDetail;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminPage;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminSearch;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminSummary;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderReference;
import com.springbootecommerce.shophappens.ordering.application.port.out.OrderRepository;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderAdministrationQueryServiceTest {

    @Mock private OrderRepository orderRepository;

    private final OrderAdminSearch search = new OrderAdminSearch(0, 20, "ORD-2026");

    @Test
    void searchOrdersReturnsTheRepositoryPage() {
        var summary =
                new OrderAdminSummary(
                        new OrderReference(java.util.UUID.randomUUID()),
                        "ORD-2026-ABC",
                        new CustomerId(7),
                        new Money(new BigDecimal("19.99")),
                        Instant.parse("2026-09-05T09:00:00Z"));
        var expected = new OrderAdminPage(java.util.List.of(summary), 0, 20, 1, 1);
        when(orderRepository.searchForAdministration(search)).thenReturn(expected);

        var service = new OrderAdministrationQueryService(orderRepository);

        assertEquals(expected, service.searchOrders(search));
        verify(orderRepository).searchForAdministration(search);
    }

    @Test
    void findOrderReturnsTheRepositoryDetail() {
        var orderNumber = "ORD-2026-ABC";
        var expectedDetail =
                new OrderAdminDetail(
                        new OrderReference(java.util.UUID.randomUUID()),
                        orderNumber,
                        new CustomerId(7),
                        new Money(new BigDecimal("19.99")),
                        Instant.parse("2026-09-05T09:00:00Z"),
                        List.of(),
                        List.of());
        Optional<OrderAdminDetail> expected = Optional.of(expectedDetail);
        when(orderRepository.findForAdministration(orderNumber)).thenReturn(expected);

        var service = new OrderAdministrationQueryService(orderRepository);

        assertEquals(expected, service.findOrder(orderNumber));
        verify(orderRepository).findForAdministration(orderNumber);
    }
}
