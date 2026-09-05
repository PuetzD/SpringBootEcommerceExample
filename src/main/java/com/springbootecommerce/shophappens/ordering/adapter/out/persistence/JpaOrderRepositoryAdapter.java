package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAddressView;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminDetail;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminPage;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminSearch;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderAdminSummary;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderItemView;
import com.springbootecommerce.shophappens.ordering.application.port.in.OrderReference;
import com.springbootecommerce.shophappens.ordering.application.port.out.OrderRepository;
import com.springbootecommerce.shophappens.ordering.domain.model.CheckoutId;
import com.springbootecommerce.shophappens.ordering.domain.model.Order;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderId;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
class JpaOrderRepositoryAdapter implements OrderRepository {
    private final SpringDataOrderRepository springData;
    private final OrderPersistenceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findByCheckout(CustomerId customerId, CheckoutId checkoutId) {
        return springData
                .findByCheckoutIdAndCustomerId(checkoutId.value(), customerId.value())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(OrderId orderId) {
        return springData.findById(orderId.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findOwnedByOrderNumber(CustomerId customerId, String orderNumber) {
        return springData
                .findByCustomerIdAndOrderNumber(customerId.value(), orderNumber)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAllByCustomer(CustomerId customerId) {
        return springData.findByCustomerIdOrderByPlacedAtDescIdDesc(customerId.value()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public Order save(Order order) {
        OrderJpaEntity entity = mapper.toJpa(order);
        return mapper.toDomain(springData.saveAndFlush(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderAdminPage searchForAdministration(OrderAdminSearch search) {
        var page =
                springData.searchForAdministration(
                        search.query(),
                        org.springframework.data.domain.PageRequest.of(
                                search.page(), search.size()));
        return new OrderAdminPage(
                page.getContent().stream().map(this::toAdminSummary).toList(),
                search.page(),
                search.size(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderAdminDetail> findForAdministration(String orderNumber) {
        return springData.findByOrderNumber(orderNumber).map(this::toAdminDetail);
    }

    private OrderAdminSummary toAdminSummary(OrderJpaEntity order) {
        return new OrderAdminSummary(
                new OrderReference(order.getId()),
                order.getOrderNumber(),
                new CustomerId(order.getCustomerId()),
                new Money(order.getTotal()),
                order.getPlacedAt());
    }

    private OrderAdminDetail toAdminDetail(OrderJpaEntity order) {
        return new OrderAdminDetail(
                new OrderReference(order.getId()),
                order.getOrderNumber(),
                new CustomerId(order.getCustomerId()),
                new Money(order.getTotal()),
                order.getPlacedAt(),
                order.getItems().stream().map(this::toItem).toList(),
                order.getAddresses().stream().map(this::toAddress).toList());
    }

    private OrderItemView toItem(OrderItemJpaEntity item) {
        return new OrderItemView(
                item.getProductId(),
                item.getSku(),
                item.getProductName(),
                new Money(item.getUnitPrice()),
                item.getQuantity(),
                new Money(item.getLineTotal()));
    }

    private OrderAddressView toAddress(OrderAddressJpaEntity address) {
        return new OrderAddressView(
                address.getAddressRole().name(),
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
}
