package com.springbootecommerce.shophappens.cart.adapter.out.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.springbootecommerce.shophappens.cart.application.port.out.GuestCartRepository;
import com.springbootecommerce.shophappens.cart.domain.exception.ConcurrentCartModificationException;
import com.springbootecommerce.shophappens.cart.domain.model.Cart;
import com.springbootecommerce.shophappens.cart.domain.model.CartId;
import com.springbootecommerce.shophappens.cart.domain.model.CartOwner;
import com.springbootecommerce.shophappens.cart.domain.model.GuestCartId;
import com.springbootecommerce.shophappens.cart.domain.model.ProductId;
import com.springbootecommerce.shophappens.cart.domain.model.Quantity;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

class RedisGuestCartRepositoryIT extends AbstractIntegrationTest {
    @Autowired GuestCartRepository repository;
    @Autowired StringRedisTemplate redis;

    @Test
    void createsLoadsAndRefreshesThirtyMinuteGuestCart() {
        GuestCartId id = GuestCartId.random();
        Cart cart = Cart.empty(CartId.random(), new CartOwner.Guest(id));
        cart.changeQuantity(new ProductId(7L), new Quantity(2));

        Cart saved = repository.save(cart, -1L);
        Cart restored = repository.find(id).orElseThrow();

        assertThat(restored.items()).isEqualTo(saved.items());
        assertThat(redis.getExpire("cart:guest:" + id.value(), TimeUnit.SECONDS))
                .isBetween(1_790L, 1_800L);
    }

    @Test
    void rejectsStaleVersionWithoutOverwriting() {
        GuestCartId id = GuestCartId.random();
        Cart initial = repository.save(guestCart(id), -1L);
        repository.save(initial, initial.version());

        assertThatThrownBy(() -> repository.save(initial, initial.version()))
                .isInstanceOf(ConcurrentCartModificationException.class);
    }

    private Cart guestCart(GuestCartId id) {
        Cart cart = Cart.empty(CartId.random(), new CartOwner.Guest(id));
        cart.changeQuantity(new ProductId(9L), new Quantity(1));
        return cart;
    }
}