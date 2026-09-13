package com.springbootecommerce.shophappens.ordering.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutItem;
import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutReview;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.json.JsonMapper;

class CheckoutReviewSessionTest {
    private static final Instant NOW = Instant.parse("2026-09-09T12:00:00Z");
    private final CheckoutReviewSession reviews =
            new CheckoutReviewSession(
                    JsonMapper.builder().findAndAddModules().build(),
                    Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void storesPrimitiveJsonAndRoundTripsTheReview() {
        MockHttpSession session = new MockHttpSession();
        UUID checkout = UUID.randomUUID();
        CheckoutReview review = review(42, NOW.plusSeconds(900));

        reviews.put(session, checkout, review);

        assertThat(session.getAttribute("ordering.review." + checkout)).isInstanceOf(String.class);
        assertThat(reviews.find(session, checkout)).contains(review);
    }

    @Test
    void expiresAtTheBoundaryAndRemovesTheStoredValue() {
        MockHttpSession session = new MockHttpSession();
        UUID checkout = UUID.randomUUID();
        reviews.put(session, checkout, review(42, NOW));

        assertThat(reviews.find(session, checkout)).isEmpty();
        assertThat(session.getAttribute("ordering.review." + checkout)).isNull();
    }

    @Test
    void keepsOnlyTheFiveReviewsWithLatestExpiry() {
        MockHttpSession session = new MockHttpSession();
        var ids = new ArrayList<UUID>();
        for (int index = 0; index < 6; index++) {
            UUID checkout = UUID.randomUUID();
            ids.add(checkout);
            reviews.put(session, checkout, review(42, NOW.plusSeconds(100 + index)));
        }

        assertThat(reviews.find(session, ids.getFirst())).isEmpty();
        assertThat(ids.subList(1, 6))
                .allSatisfy(id -> assertThat(reviews.find(session, id)).isPresent());
    }

    @Test
    void reviewIsSessionScopedRetainsCustomerAndCanBeRemoved() {
        MockHttpSession original = new MockHttpSession();
        MockHttpSession other = new MockHttpSession();
        UUID checkout = UUID.randomUUID();
        reviews.put(original, checkout, review(99, NOW.plusSeconds(900)));

        assertThat(reviews.find(other, checkout)).isEmpty();
        assertThat(reviews.find(original, UUID.randomUUID())).isEmpty();
        assertThat(reviews.find(original, checkout).orElseThrow().customer())
                .isEqualTo(new CustomerId(99));

        reviews.remove(original, checkout);
        assertThat(reviews.find(original, checkout)).isEmpty();
    }

    private static CheckoutReview review(long customer, Instant expiresAt) {
        return new CheckoutReview(
                new CustomerId(customer),
                java.util.List.of(
                        new CheckoutItem(
                                new ProductVariantId(202),
                                new ProductId(7),
                                "BLUE",
                                "Blue shirt",
                                new Money(new BigDecimal("20.00")),
                                3)),
                expiresAt);
    }
}
