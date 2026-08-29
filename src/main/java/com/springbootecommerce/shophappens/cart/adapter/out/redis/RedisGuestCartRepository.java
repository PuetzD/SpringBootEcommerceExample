package com.springbootecommerce.shophappens.cart.adapter.out.redis;

import com.springbootecommerce.shophappens.cart.application.port.out.GuestCartRepository;
import com.springbootecommerce.shophappens.cart.domain.model.Cart;
import com.springbootecommerce.shophappens.cart.domain.model.CartId;
import com.springbootecommerce.shophappens.cart.domain.model.CartOwner;
import com.springbootecommerce.shophappens.cart.domain.model.GuestCartId;
import com.springbootecommerce.shophappens.cart.domain.model.ProductId;
import com.springbootecommerce.shophappens.cart.domain.model.Quantity;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Repository
class RedisGuestCartRepository implements GuestCartRepository {
    private static final long GUEST_CART_TTL_SECONDS = 1800;

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    RedisGuestCartRepository(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<Cart> find(GuestCartId id) {
        String payload = redis.opsForValue().get(keyOf(id));
        if (payload == null) {
            return Optional.empty();
        }
        return Optional.of(restore(id, readDocument(payload)));
    }

    @Override
    public Cart save(Cart cart) {
        GuestCartId guestId = guestIdOf(cart);
        GuestCartDocument document =
                new GuestCartDocument(cart.id().value(), guestId.value(), itemsOf(cart));
        redis.opsForValue()
                .set(
                        keyOf(guestId),
                        writeDocument(document),
                        GUEST_CART_TTL_SECONDS,
                        TimeUnit.SECONDS);
        return restore(guestId, document);
    }

    @Override
    public void delete(GuestCartId id) {
        redis.delete(keyOf(id));
    }

    private static String keyOf(GuestCartId id) {
        return "cart:guest:" + id.value();
    }

    private static List<GuestCartDocument.Item> itemsOf(Cart cart) {
        return cart.items().stream()
                .map(
                        item ->
                                new GuestCartDocument.Item(
                                        item.productId().value(), item.quantity().value()))
                .toList();
    }

    private static GuestCartId guestIdOf(Cart cart) {
        if (!(cart.owner() instanceof CartOwner.Guest guest)) {
            throw new IllegalArgumentException("Only Guest Carts can be persisted to Redis");
        }
        return guest.id();
    }

    private static Cart restore(GuestCartId guestId, GuestCartDocument document) {
        Cart cart = Cart.restore(new CartId(document.cartId()), new CartOwner.Guest(guestId), 0);
        document.items()
                .forEach(
                        item ->
                                cart.changeQuantity(
                                        new ProductId(item.productId()),
                                        new Quantity(item.quantity())));
        return cart;
    }

    private String writeDocument(GuestCartDocument document) {
        try {
            return objectMapper.writeValueAsString(document);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize Guest Cart document", e);
        }
    }

    private GuestCartDocument readDocument(String payload) {
        try {
            return objectMapper.readValue(
                    payload.getBytes(StandardCharsets.UTF_8), GuestCartDocument.class);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to deserialize Guest Cart document", e);
        }
    }
}
