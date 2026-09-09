package com.springbootecommerce.shophappens.ordering.adapter.in.web;

import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutItem;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutReview;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
class CheckoutReviewSession {
    private static final String ATTRIBUTE_PREFIX = "ordering.review.";
    private static final int MAX_REVIEWS = 5;

    private final ObjectMapper objectMapper;
    private final Clock clock;

    void put(HttpSession session, UUID checkoutId, CheckoutReview review) {
        removeExpired(session);
        session.setAttribute(attribute(checkoutId), write(ReviewDocument.from(review)));
        stored(session).stream()
                .sorted(Comparator.comparing(entry -> entry.review().expiresAt()))
                .limit(Math.max(0, stored(session).size() - MAX_REVIEWS))
                .forEach(entry -> session.removeAttribute(entry.attribute()));
    }

    Optional<CheckoutReview> find(HttpSession session, UUID checkoutId) {
        String attribute = attribute(checkoutId);
        Object payload = session.getAttribute(attribute);
        if (!(payload instanceof String json)) {
            return Optional.empty();
        }
        Optional<ReviewDocument> document = read(json);
        if (document.isEmpty() || !clock.instant().isBefore(document.get().expiresAt())) {
            session.removeAttribute(attribute);
            return Optional.empty();
        }
        return document.map(ReviewDocument::toReview);
    }

    void remove(HttpSession session, UUID checkoutId) {
        session.removeAttribute(attribute(checkoutId));
    }

    private void removeExpired(HttpSession session) {
        stored(session).stream()
                .filter(entry -> !clock.instant().isBefore(entry.review().expiresAt()))
                .forEach(entry -> session.removeAttribute(entry.attribute()));
    }

    private List<StoredReview> stored(HttpSession session) {
        return Collections.list(session.getAttributeNames()).stream()
                .filter(name -> name.startsWith(ATTRIBUTE_PREFIX))
                .map(name -> stored(session, name))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<StoredReview> stored(HttpSession session, String attribute) {
        Object payload = session.getAttribute(attribute);
        if (!(payload instanceof String json)) {
            return Optional.empty();
        }
        Optional<ReviewDocument> review = read(json);
        if (review.isEmpty()) {
            session.removeAttribute(attribute);
        }
        return review.map(document -> new StoredReview(attribute, document));
    }

    private String write(ReviewDocument review) {
        try {
            return objectMapper.writeValueAsString(review);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Failed to serialize checkout review", exception);
        }
    }

    private Optional<ReviewDocument> read(String payload) {
        try {
            return Optional.of(objectMapper.readValue(payload, ReviewDocument.class));
        } catch (JacksonException exception) {
            return Optional.empty();
        }
    }

    private static String attribute(UUID checkoutId) {
        return ATTRIBUTE_PREFIX + checkoutId;
    }

    private record StoredReview(String attribute, ReviewDocument review) {}

    private record ReviewDocument(long customerId, List<ItemDocument> items, Instant expiresAt) {
        static ReviewDocument from(CheckoutReview review) {
            return new ReviewDocument(
                    review.customer().value(),
                    review.items().stream().map(ItemDocument::from).toList(),
                    review.expiresAt());
        }

        CheckoutReview toReview() {
            return new CheckoutReview(
                    new CustomerId(customerId),
                    items.stream().map(ItemDocument::toItem).toList(),
                    expiresAt);
        }
    }

    private record ItemDocument(
            long variantId,
            long productId,
            String sku,
            String productName,
            BigDecimal unitPrice,
            int quantity) {
        static ItemDocument from(CheckoutItem item) {
            return new ItemDocument(
                    item.variant().value(),
                    item.product().value(),
                    item.sku(),
                    item.productName(),
                    item.unitPrice().amount(),
                    item.quantity());
        }

        CheckoutItem toItem() {
            return new CheckoutItem(
                    new ProductVariantId(variantId),
                    new ProductId(productId),
                    sku,
                    productName,
                    new Money(unitPrice),
                    quantity);
        }
    }
}
