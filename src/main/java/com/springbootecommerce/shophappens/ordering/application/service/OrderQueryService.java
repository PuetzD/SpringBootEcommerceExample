package com.springbootecommerce.shophappens.ordering.application.service;

import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutAddress;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutItem;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutPreparation;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAddressView;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderDetail;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderItemView;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderQuery;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderReference;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderSummary;
import com.springbootecommerce.shophappens.ordering.application.port.in.PrepareCheckoutUseCase;
import com.springbootecommerce.shophappens.ordering.application.port.out.CheckoutCatalogGateway;
import com.springbootecommerce.shophappens.ordering.application.port.out.CustomerAddressGateway;
import com.springbootecommerce.shophappens.ordering.application.port.out.CustomerCartGateway;
import com.springbootecommerce.shophappens.ordering.application.port.out.OrderRepository;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import java.util.ArrayList;
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
    private final CheckoutCatalogGateway catalog;

    @Override
    public CheckoutPreparation prepare(CustomerId cid) {
        List<CheckoutItem> items = new ArrayList<>();
        List<ProductVariantId> unavailableVariants = new ArrayList<>();
        carts.load(cid)
                .products()
                .forEach(
                        product ->
                                catalog.find(product.variantId(), product.quantity())
                                        .ifPresentOrElse(
                                                items::add,
                                                () ->
                                                        unavailableVariants.add(
                                                                product.variantId())));
        List<CheckoutAddress> addressList =
                addresses.available(cid).stream()
                        .map(
                                a ->
                                        new CheckoutAddress(
                                                a.addressId(),
                                                a.recipientName(),
                                                a.city(),
                                                a.postalCode(),
                                                a.countryCode(),
                                                a.defaultShipping(),
                                                a.defaultBilling()))
                        .toList();
        return new CheckoutPreparation(cid, items, addressList, unavailableVariants);
    }

    @Override
    public Optional<OrderDetail> findOwned(CustomerId customer, String orderNumber) {
        return orders.findOwnedByOrderNumber(customer, orderNumber)
                .map(
                        o ->
                                new OrderDetail(
                                        new OrderReference(o.orderId().value()),
                                        o.orderNumber().value(),
                                        o.total(),
                                        o.placedAt(),
                                        o.items().stream()
                                                .map(
                                                        i ->
                                                                new OrderItemView(
                                                                        i.variantId().value(),
                                                                        i.productId().value(),
                                                                        i.sku(),
                                                                        i.productName(),
                                                                        i.unitPrice(),
                                                                        i.quantity(),
                                                                        i.lineTotal()))
                                                .toList(),
                                        List.of(o.shippingAddress(), o.billingAddress()).stream()
                                                .map(
                                                        a ->
                                                                new OrderAddressView(
                                                                        a.role().name(),
                                                                        a.recipientName(),
                                                                        a.companyName(),
                                                                        a.addressLine1(),
                                                                        a.addressLine2(),
                                                                        a.city(),
                                                                        a.region(),
                                                                        a.postalCode(),
                                                                        a.countryCode(),
                                                                        a.phoneNumber()))
                                                .toList()));
    }

    @Override
    public List<OrderSummary> findAll(CustomerId customer) {
        return orders.findAllByCustomer(customer).stream()
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
