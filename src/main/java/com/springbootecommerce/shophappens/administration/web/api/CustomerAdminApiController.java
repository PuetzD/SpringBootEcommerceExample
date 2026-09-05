package com.springbootecommerce.shophappens.administration.web.api;

import com.springbootecommerce.shophappens.customer.application.CustomerNotFoundException;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdminAddressView;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdminDetail;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdminSearch;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdminSummary;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdministrationQuery;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminSummary;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdministrationQuery;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/customers")
public class CustomerAdminApiController {
    private final CustomerAdministrationQuery customerAdministrationQuery;
    private final OrderAdministrationQuery orderAdministrationQuery;

    @GetMapping
    public PageResponse<CustomerResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String q) {
        var result =
                customerAdministrationQuery.searchCustomers(new CustomerAdminSearch(page, size, q));
        return new PageResponse<>(
                result.content().stream().map(this::toListResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }

    @GetMapping("/{customerId}")
    public CustomerResponse detail(@PathVariable @Positive long customerId) {
        var id = new CustomerId(customerId);
        var customer =
                customerAdministrationQuery
                        .findCustomer(id)
                        .orElseThrow(
                                () ->
                                        new CustomerNotFoundException(
                                                new CustomerReference(customerId)));
        var orders = orderAdministrationQuery.findOrdersForCustomer(id);
        return toDetailResponse(customer, orders);
    }

    private CustomerResponse toListResponse(CustomerAdminSummary customer) {
        return new CustomerResponse(
                customer.customerId().value(),
                customer.givenName(),
                customer.familyName(),
                customer.contactEmail(),
                null,
                List.of(),
                List.of());
    }

    private CustomerResponse toDetailResponse(
            CustomerAdminDetail customer, List<OrderAdminSummary> orders) {
        return new CustomerResponse(
                customer.customerId().value(),
                customer.givenName(),
                customer.familyName(),
                customer.contactEmail(),
                customer.accountId().value(),
                customer.addresses().stream().map(this::toAddressResponse).toList(),
                orders.stream().map(this::toOrderResponse).toList());
    }

    private CustomerAddressResponse toAddressResponse(CustomerAdminAddressView address) {
        return new CustomerAddressResponse(
                address.address().value(),
                address.recipientName(),
                address.companyName(),
                address.addressLine1(),
                address.addressLine2(),
                address.city(),
                address.region(),
                address.postalCode(),
                address.countryCode(),
                address.phoneNumber(),
                address.defaultShipping(),
                address.defaultBilling());
    }

    private CustomerOrderResponse toOrderResponse(OrderAdminSummary order) {
        return new CustomerOrderResponse(
                order.orderNumber(),
                order.order().value(),
                order.total().amount(),
                order.placedAt(),
                "/admin/orders/" + order.orderNumber());
    }
}
