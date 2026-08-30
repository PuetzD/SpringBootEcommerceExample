package com.springbootecommerce.shophappens.ordering.application.service;

import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutAddress;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutItem;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutPreparation;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderDetail;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderQuery;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderReference;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderSummary;
import com.springbootecommerce.shophappens.ordering.application.port.in.PrepareCheckoutUseCase;
import com.springbootecommerce.shophappens.ordering.application.port.out.CustomerAddressGateway;
import com.springbootecommerce.shophappens.ordering.application.port.out.CustomerCartGateway;
import com.springbootecommerce.shophappens.ordering.application.port.out.OrderRepository;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService implements PrepareCheckoutUseCase, OrderQuery {
    private final CustomerCartGateway carts;
    private final CustomerAddressGateway addresses;
    private final OrderRepository orders;

    @Override
    public CheckoutPreparation prepare(CustomerReference customer) {
        CustomerId cid = new CustomerId(customer.value());
        List<CheckoutItem> items =
                carts.load(cid).products().stream()
                        .map(
                                p ->
                                        new CheckoutItem(
                                                new ProductReference(p.productId().value()),
                                                p.quantity()))
                        .toList();
        List<CheckoutAddress> addressList =
                addresses.available(cid).stream()
                        .map(
                                a ->
                                        new CheckoutAddress(
                                                a.address(),
                                                a.recipientName(),
                                                a.city(),
                                                a.postalCode(),
                                                a.countryCode(),
                                                a.defaultShipping(),
                                                a.defaultBilling()))
                        .toList();
        return new CheckoutPreparation(customer, items, addressList);
    }

    @Override
    public Optional<OrderDetail> findOwned(CustomerReference customer, String orderNumber) {
        return orders.findAllByCustomer(new CustomerId(customer.value())).stream()
                .filter(o -> o.orderNumber().value().equals(orderNumber))
                .findFirst()
                .map(
                        o ->
                                new OrderDetail(
                                        new OrderReference(o.orderId().value()),
                                        o.orderNumber().value(),
                                        o.total(),
                                        o.placedAt(),
                                        List.copyOf(o.items()),
                                        List.of(o.shippingAddress(), o.billingAddress())));
    }

    @Override
    public List<OrderSummary> findAll(CustomerReference customer) {
        return orders.findAllByCustomer(new CustomerId(customer.value())).stream()
                .map(
                        o ->
                                new OrderSummary(
                                        new OrderReference(o.orderId().value()),
                                        o.orderNumber().value(),
                                        o.total(),
                                        o.placedAt()))
                .toList();
    }
}
