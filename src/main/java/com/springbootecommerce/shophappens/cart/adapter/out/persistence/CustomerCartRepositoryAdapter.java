package com.springbootecommerce.shophappens.cart.adapter.out.persistence;

import com.springbootecommerce.shophappens.cart.application.port.out.CustomerCartRepository;
import com.springbootecommerce.shophappens.cart.domain.model.Cart;
import com.springbootecommerce.shophappens.cart.domain.model.CartId;
import com.springbootecommerce.shophappens.cart.domain.model.CartOwner;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Repository
class CustomerCartRepositoryAdapter implements CustomerCartRepository {
    private final SpringDataCustomerCartRepository springData;
    private final CustomerCartPersistenceMapper mapper;

    @Override
    @Transactional
    public Cart findOrCreate(CustomerId customerId) {
        return springData
                .findWithItemsByCustomerId(customerId.value())
                .map(mapper::toDomain)
                .orElseGet(
                        () ->
                                save(
                                        Cart.empty(
                                                CartId.random(),
                                                new CartOwner.Customer(customerId))));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cart> find(CustomerId customerId) {
        return springData.findWithItemsByCustomerId(customerId.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Cart save(Cart cart) {
        long customerId = customerIdOf(cart);
        CustomerCartJpaEntity entity =
                springData
                        .findByCustomerId(customerId)
                        .orElseGet(
                                () -> CustomerCartJpaEntity.create(cart.id().value(), customerId));
        entity.setItems(mapper.toJpaItems(entity, cart));
        return mapper.toDomain(springData.saveAndFlush(entity));
    }

    @Override
    @Transactional
    public void clear(CustomerId customerId) {
        springData.findWithItemsByCustomerId(customerId.value()).ifPresent(springData::delete);
    }

    private long customerIdOf(Cart cart) {
        if (!(cart.owner() instanceof CartOwner.Customer customer)) {
            throw new IllegalArgumentException("Only Customer Carts can be persisted");
        }
        return customer.id().value();
    }
}
