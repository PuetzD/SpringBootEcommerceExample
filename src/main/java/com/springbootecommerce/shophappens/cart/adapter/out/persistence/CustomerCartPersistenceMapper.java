package com.springbootecommerce.shophappens.cart.adapter.out.persistence;

import com.springbootecommerce.shophappens.cart.domain.model.Cart;
import com.springbootecommerce.shophappens.cart.domain.model.CartId;
import com.springbootecommerce.shophappens.cart.domain.model.CartItem;
import com.springbootecommerce.shophappens.cart.domain.model.CartOwner;
import com.springbootecommerce.shophappens.cart.domain.model.CustomerId;
import com.springbootecommerce.shophappens.cart.domain.model.ProductId;
import com.springbootecommerce.shophappens.cart.domain.model.Quantity;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class CustomerCartPersistenceMapper {
    Cart toDomain(CustomerCartJpaEntity jpa) {
        Cart cart =
                Cart.restore(
                        new CartId(jpa.getId()),
                        new CartOwner.Customer(new CustomerId(jpa.getCustomerId())),
                        jpa.getVersion());
        for (CustomerCartItemJpaEntity item : jpa.getItems()) {
            cart.changeQuantity(
                    new ProductId(item.getProductId()), new Quantity(item.getQuantity()));
        }
        return cart;
    }

    List<CustomerCartItemJpaEntity> toJpaItems(CustomerCartJpaEntity owner, Cart cart) {
        return cart.items().stream().map(item -> toJpaItem(owner, item)).toList();
    }

    private CustomerCartItemJpaEntity toJpaItem(CustomerCartJpaEntity owner, CartItem item) {
        return CustomerCartItemJpaEntity.create(
                owner, item.productId().value(), item.quantity().value());
    }
}
