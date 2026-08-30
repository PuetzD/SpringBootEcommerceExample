package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import com.springbootecommerce.shophappens.ordering.domain.model.CheckoutId;
import com.springbootecommerce.shophappens.ordering.domain.model.Order;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderAddress;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderId;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderItem;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderNumber;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class OrderPersistenceMapper {

    Order toDomain(OrderJpaEntity jpa) {
        List<OrderItem> items =
                jpa.getItems().stream()
                        .sorted(Comparator.comparingInt(OrderItemJpaEntity::getLineNumber))
                        .map(
                                item ->
                                        new OrderItem(
                                                new ProductId(item.getProductId()),
                                                item.getSku(),
                                                item.getProductName(),
                                                new Money(item.getUnitPrice()),
                                                item.getQuantity()))
                        .toList();
        OrderAddress shipping =
                jpa.getAddresses().stream()
                        .filter(
                                a ->
                                        a.getAddressRole()
                                                == com.springbootecommerce.shophappens.ordering
                                                        .domain.model.AddressRole.SHIPPING)
                        .findFirst()
                        .map(this::toDomainAddress)
                        .orElseThrow();
        OrderAddress billing =
                jpa.getAddresses().stream()
                        .filter(
                                a ->
                                        a.getAddressRole()
                                                == com.springbootecommerce.shophappens.ordering
                                                        .domain.model.AddressRole.BILLING)
                        .findFirst()
                        .map(this::toDomainAddress)
                        .orElseThrow();
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
                OrderJpaEntity.fromId(
                        order.orderId().value(),
                        order.orderNumber().value(),
                        order.checkoutId().value(),
                        order.customerId().value(),
                        order.total().amount(),
                        order.placedAt());
        List<OrderItemJpaEntity> itemEntities = new java.util.ArrayList<>();
        for (int i = 0; i < order.items().size(); i++) {
            OrderItem item = order.items().get(i);
            itemEntities.add(
                    OrderItemJpaEntity.create(
                            entity,
                            i,
                            item.productId().value(),
                            item.sku(),
                            item.productName(),
                            item.unitPrice().amount(),
                            item.quantity(),
                            item.lineTotal().amount()));
        }
        entity.setItems(itemEntities);

        List<OrderAddressJpaEntity> addressEntities =
                List.of(
                        toJpaAddress(entity, order.shippingAddress()),
                        toJpaAddress(entity, order.billingAddress()));
        entity.setAddresses(addressEntities);
        return entity;
    }

    private OrderAddress toDomainAddress(OrderAddressJpaEntity address) {
        return new OrderAddress(
                address.getAddressRole(),
                address.getRecipientName(),
                address.getCompanyName(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getRegion(),
                address.getPostalCode(),
                address.getCountryCode(),
                address.getPhoneNumber());
    }

    private OrderAddressJpaEntity toJpaAddress(OrderJpaEntity entity, OrderAddress address) {
        return OrderAddressJpaEntity.create(
                entity,
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
