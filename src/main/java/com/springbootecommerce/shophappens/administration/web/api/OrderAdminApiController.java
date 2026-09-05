package com.springbootecommerce.shophappens.administration.web.api;

import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAddressView;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminDetail;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminSearch;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminSummary;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdministrationQuery;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderItemView;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/orders")
public class OrderAdminApiController {
    private final OrderAdministrationQuery orders;

    @GetMapping
    public PageResponse<OrderResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q) {
        var result = orders.searchOrders(new OrderAdminSearch(page, size, q));
        return new PageResponse<>(
                result.content().stream().map(this::toResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }

    @GetMapping("/{orderNumber}")
    public OrderResponse detail(@PathVariable String orderNumber) {
        return orders.findOrder(orderNumber)
                .map(this::toResponse)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));
    }

    private OrderResponse toResponse(OrderAdminSummary order) {
        return new OrderResponse(
                order.orderNumber(),
                order.order().value(),
                order.orderNumber(),
                order.customerId().value(),
                order.total().amount(),
                order.placedAt(),
                List.of(),
                List.of());
    }

    private OrderResponse toResponse(OrderAdminDetail order) {
        return new OrderResponse(
                order.orderNumber(),
                order.order().value(),
                order.orderNumber(),
                order.customerId().value(),
                order.total().amount(),
                order.placedAt(),
                order.items().stream().map(this::toResponse).toList(),
                order.addresses().stream().map(this::toResponse).toList());
    }

    private OrderItemResponse toResponse(OrderItemView item) {
        return new OrderItemResponse(
                item.productId(),
                item.sku(),
                item.productName(),
                item.unitPrice().amount(),
                item.quantity(),
                item.lineTotal().amount());
    }

    private OrderAddressResponse toResponse(OrderAddressView address) {
        return new OrderAddressResponse(
                address.role(),
                address.recipientName(),
                address.companyName(),
                address.addressLine1(),
                address.addressLine2(),
                address.city(),
                address.region(),
                address.postalCode(),
                address.countryCode(),
                address.phoneNumber());
    }
}
