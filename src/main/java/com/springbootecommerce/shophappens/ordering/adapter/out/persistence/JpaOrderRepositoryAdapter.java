package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import com.springbootecommerce.shophappens.ordering.application.port.out.OrderRepository;
import com.springbootecommerce.shophappens.ordering.domain.model.CheckoutId;
import com.springbootecommerce.shophappens.ordering.domain.model.CustomerId;
import com.springbootecommerce.shophappens.ordering.domain.model.Order;
import com.springbootecommerce.shophappens.ordering.domain.model.OrderId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
class JpaOrderRepositoryAdapter implements OrderRepository {
    private final SpringDataOrderRepository springData;
    private final OrderPersistenceMapper mapper;

    JpaOrderRepositoryAdapter(SpringDataOrderRepository springData, OrderPersistenceMapper mapper) {
        this.springData = springData;
        this.mapper = mapper;
    }

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
    public List<Order> findAllByCustomer(CustomerId customerId) {
        return springData.findByCustomerId(customerId.value()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public Order save(Order order) {
        OrderJpaEntity entity = mapper.toJpa(order);
        return mapper.toDomain(springData.saveAndFlush(entity));
    }
}
