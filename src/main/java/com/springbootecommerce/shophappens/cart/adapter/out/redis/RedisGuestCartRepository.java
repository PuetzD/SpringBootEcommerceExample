package com.springbootecommerce.shophappens.cart.adapter.out.redis;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.springbootecommerce.shophappens.cart.application.port.out.GuestCartRepository;
import com.springbootecommerce.shophappens.cart.domain.exception.ConcurrentCartModificationException;
import com.springbootecommerce.shophappens.cart.domain.model.Cart;
import com.springbootecommerce.shophappens.cart.domain.model.CartId;
import com.springbootecommerce.shophappens.cart.domain.model.CartOwner;
import com.springbootecommerce.shophappens.cart.domain.model.GuestCartId;
import com.springbootecommerce.shophappens.cart.domain.model.ProductId;
import com.springbootecommerce.shophappens.cart.domain.model.Quantity;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

@Repository
class RedisGuestCartRepository implements GuestCartRepository {
    private static final int GUEST_CART_TTL_SECONDS = 1800;

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final DefaultRedisScript<Long> saveScript;

    RedisGuestCartRepository(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.saveScript = new DefaultRedisScript<>();
        this.saveScript.setLocation(new ClassPathResource("redis/save-guest-cart.lua"));
        this.saveScript.setResultType(Long.class);
    }

    @Override
    public Optional<Cart> find(GuestCartId id) {
        String key = keyOf(id);
        Map<Object, Object> entries = redis.opsForHash().entries(key);
        if (entries.isEmpty()) {
            return Optional.empty();
        }
        Object version = entries.get("version");
        Object payload = entries.get("payload");
        if (version == null || payload == null) {
            return Optional.empty();
        }
        GuestCartDocument document = readDocument(payload.toString());
        Cart cart =
                Cart.restore(
                        new CartId(document.cartId()),
                        new CartOwner.Guest(id),
                        version(document));
        document.items().forEach(item -> cart.changeQuantity(new ProductId(item.productId()), new Quantity(item.quantity())));
        return Optional.of(cart);
    }

    @Override
    public Cart save(Cart cart, long expectedVersion) {
        GuestCartId guestId = guestIdOf(cart);
        long nextVersion = Math.addExact(cart.version(), 1);
        GuestCartDocument document =
                new GuestCartDocument(
                        cart.id().value(),
                        guestId.value(),
                        nextVersion,
                        itemsOf(cart));
        Long result =
                redis.execute(
                        saveScript,
                        List.of(keyOf(guestId)),
                        String.valueOf(expectedVersion),
                        String.valueOf(nextVersion),
                        writeDocument(document),
                        String.valueOf(GUEST_CART_TTL_SECONDS));
        if (result == null || result != 1) {
            throw new ConcurrentCartModificationException(
                    "Guest cart has been concurrently modified: " + guestId.value());
        }
        return restore(guestId, document);
    }

    @Override
    public void delete(GuestCartId id) {
        redis.delete(keyOf(id));
    }

    private static String keyOf(GuestCartId id) {
        return "cart:guest:" + id.value();
    }

    private static long version(GuestCartDocument document) {
        return document.version();
    }

    private static List<GuestCartDocument.Item> itemsOf(Cart cart) {
        return cart.items().stream()
                .map(item -> new GuestCartDocument.Item(item.productId().value(), item.quantity().value()))
                .toList();
    }

    private static GuestCartId guestIdOf(Cart cart) {
        if (!(cart.owner() instanceof CartOwner.Guest guest)) {
            throw new IllegalArgumentException("Only Guest Carts can be persisted to Redis");
        }
        return guest.id();
    }

    private static Cart restore(GuestCartId guestId, GuestCartDocument document) {
        Cart cart =
                Cart.restore(
                        new CartId(document.cartId()),
                        new CartOwner.Guest(guestId),
                        document.version());
        document.items().forEach(item -> cart.changeQuantity(new ProductId(item.productId()), new Quantity(item.quantity())));
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
            return objectMapper.readValue(payload.getBytes(StandardCharsets.UTF_8), GuestCartDocument.class);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to deserialize Guest Cart document", e);
        }
    }
}