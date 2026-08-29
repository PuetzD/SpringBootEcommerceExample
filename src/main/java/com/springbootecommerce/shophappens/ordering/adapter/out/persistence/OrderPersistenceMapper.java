package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import com.springbootecommerce.shophappens.ordering.domain.model.AddressRole;
import com.springbootecommerce.shophappens.ordering.domain.model.CheckoutId;
import com.springbootecommerce.shophappens.ordering.domain.model.CustomerId;
import com.springbootecommerce.shophappens.ordering.domain.model.Order;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderAddress;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderId;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderItem;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderNumber;
import com.springbootecommerce.shophappens.ordering.domain.model.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class OrderPersistenceMapper {

    Order toDomain(OrderJpaEntity jpa) {
        List<OrderItem> items =
                jpa.getItems().stream()
                        .map(
                                item ->
                                        new OrderItem(
                                                new ProductId(item.getProductId()),
                                                item.getSku(),
                                                item.getName(),
                                                new Money(item.getUnitPrice()),
                                                item.getQuantity()))
                        .toList();
        OrderAddress shipping =
                new OrderAddress(
                        AddressRole.SHIPPING,
                        jpa.getShippingRecipientName(),
                        jpa.getShippingCompanyName(),
                        jpa.getShippingAddressLine1(),
                        jpa.getShippingAddressLine2(),
                        jpa.getShippingCity(),
                        jpa.getShippingRegion(),
                        jpa.getShippingPostalCode(),
                        jpa.getShippingCountryCode(),
                        jpa.getShippingPhoneNumber());
        OrderAddress billing =
                new OrderAddress(
                        AddressRole.BILLING,
                        jpa.getBillingRecipientName(),
                        jpa.getBillingCompanyName(),
                        jpa.getBillingAddressLine1(),
                        jpa.getBillingAddressLine2(),
                        jpa.getBillingCity(),
                        jpa.getBillingRegion(),
                        jpa.getBillingPostalCode(),
                        jpa.getBillingCountryCode(),
                        jpa.getBillingPhoneNumber());
        return Order.restore(
                new OrderId(jpa.getId()),
                new OrderNumber(jpa.getOrderNumber()),
                new CheckoutId(jpa.getCheckoutId()),
                new CustomerId(jpa.getCustomerId()),
                items,
                shipping,
                billing,
                jpa.getPlacedAt(),
                new Money(jpa.getTotal()));
    }

    OrderJpaEntity toJpa(Order order) {
        var entity =
                OrderJpaEntity.create(
                        order.orderId().value(),
                        order.orderNumber().value(),
                        order.checkoutId().value(),
                        order.customerId().value(),
                        order.shippingAddress().recipientName(),
                        order.shippingAddress().companyName(),
                        order.shippingAddress().addressLine1(),
                        order.shippingAddress().addressLine2(),
                        order.shippingAddress().city(),
                        order.shippingAddress().region(),
                        order.shippingAddress().postalCode(),
                        order.shippingAddress().countryCode(),
                        order.shippingAddress().phoneNumber(),
                        order.billingAddress().recipientName(),
                        order.billingAddress().companyName(),
                        order.billingAddress().addressLine1(),
                        order.billingAddress().addressLine2(),
                        order.billingAddress().city(),
                        order.billingAddress().region(),
                        order.billingAddress().postalCode(),
                        order.billingAddress().countryCode(),
                        order.billingAddress().phoneNumber(),
                        order.placedAt(),
                        order.total().amount());
        List<OrderItemJpaEntity> itemEntities =
                order.items().stream()
                        .map(
                                item ->
                                        OrderItemJpaEntity.create(
                                                entity,
                                                item.productId().value(),
                                                item.sku(),
                                                item.productName(),
                                                item.unitPrice().amount(),
                                                item.quantity()))
                        .toList();
        entity.setItems(itemEntities);
        return entity;
    }
}
