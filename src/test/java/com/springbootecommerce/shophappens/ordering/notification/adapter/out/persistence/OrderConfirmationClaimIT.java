package com.springbootecommerce.shophappens.ordering.notification.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;

import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.ordering.application.event.OrderPlacedIntegrationEvent;
import com.springbootecommerce.shophappens.ordering.notification.application.OrderConfirmationDeliveryService;
import com.springbootecommerce.shophappens.ordering.notification.application.port.in.OrderConfirmationPendingException;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationClaim;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationDelivery;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.StaleOrderConfirmationClaimException;
import com.springbootecommerce.shophappens.sharedkernel.money.Currency;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

class OrderConfirmationClaimIT extends AbstractIntegrationTest {
    private static final String ORDER_NUMBER = "ORD-2026-100001";
    private static final Instant NOW = Instant.parse("2026-09-13T10:00:00Z");

    @Autowired OrderConfirmationDelivery deliveries;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactions;
    @MockitoSpyBean Clock clock;

    @BeforeEach
    void setTime() {
        doReturn(NOW).when(clock).instant();
    }

    @Test
    void futureRetryMustNotReturnSuccessfullyToTheListener() {
        UUID eventId = UUID.randomUUID();
        claim(eventId);
        deliveries.markFailed(
                eventId, token(eventId), "mail unavailable", NOW.plusSeconds(8), false);
        var service =
                new OrderConfirmationDeliveryService(
                        deliveries, event -> null, message -> {}, clock);

        assertThatThrownBy(() -> service.send(event(eventId)))
                .isInstanceOf(OrderConfirmationPendingException.class);
    }

    @Test
    void liveLeaseMustNotReturnSuccessfullyToTheListener() {
        UUID eventId = UUID.randomUUID();
        claim(eventId);
        var service =
                new OrderConfirmationDeliveryService(
                        deliveries, event -> null, message -> {}, clock);

        assertThatThrownBy(() -> service.send(event(eventId)))
                .isInstanceOf(OrderConfirmationPendingException.class);
    }

    @Test
    void staleFailureCannotOverwriteReplacementSentOutcome() {
        UUID eventId = UUID.randomUUID();
        claim(eventId);
        UUID staleToken = token(eventId);
        doReturn(NOW.plusSeconds(300)).when(clock).instant();
        claim(eventId);
        deliveries.markSent(eventId, token(eventId), clock.instant());

        assertThatThrownBy(
                        () ->
                                deliveries.markFailed(
                                        eventId, staleToken, "stale failure", NOW, false))
                .isInstanceOf(StaleOrderConfirmationClaimException.class);
        assertThat(row(eventId)).containsEntry("status", "SENT");
    }

    @Test
    void staleSuccessCannotOverwriteReplacementFailureOutcome() {
        UUID eventId = UUID.randomUUID();
        claim(eventId);
        UUID staleToken = token(eventId);
        doReturn(NOW.plusSeconds(300)).when(clock).instant();
        claim(eventId);
        deliveries.markFailed(
                eventId, token(eventId), "replacement failure", NOW.plusSeconds(301), false);

        assertThatThrownBy(() -> deliveries.markSent(eventId, staleToken, NOW))
                .isInstanceOf(StaleOrderConfirmationClaimException.class);
        assertThat(row(eventId)).containsEntry("status", "FAILED");
    }

    @ParameterizedTest
    @ValueSource(strings = {"SENT", "FAILED", "QUARANTINED"})
    void staleOutcomeCannotCompleteWhileReplacementOwnsClaim(String outcome) {
        UUID eventId = UUID.randomUUID();
        claim(eventId);
        UUID staleToken = token(eventId);
        doReturn(NOW.plusSeconds(300)).when(clock).instant();
        claim(eventId);

        assertThat(token(eventId)).isNotEqualTo(staleToken);
        assertThatThrownBy(
                        () -> {
                            if (outcome.equals("SENT")) {
                                deliveries.markSent(eventId, staleToken, NOW);
                            } else {
                                deliveries.markFailed(
                                        eventId,
                                        staleToken,
                                        "stale failure",
                                        NOW,
                                        outcome.equals("QUARANTINED"));
                            }
                        })
                .isInstanceOf(StaleOrderConfirmationClaimException.class);
        assertThat(row(eventId)).containsEntry("status", "CLAIMED");
    }

    @ParameterizedTest
    @ValueSource(strings = {"SENT", "FAILED", "QUARANTINED"})
    void completedTokenCannotRecordASecondOutcome(String outcome) {
        UUID eventId = UUID.randomUUID();
        var acquired = (OrderConfirmationClaim.Acquired) claim(eventId);
        assertThat(token(eventId)).isEqualTo(acquired.claimToken());
        if (outcome.equals("SENT")) {
            deliveries.markSent(eventId, acquired.claimToken(), NOW);
        } else {
            deliveries.markFailed(
                    eventId, acquired.claimToken(), "failure", NOW, outcome.equals("QUARANTINED"));
        }
        var completed = row(eventId);

        assertThatThrownBy(() -> deliveries.markSent(eventId, acquired.claimToken(), NOW))
                .isInstanceOf(StaleOrderConfirmationClaimException.class);
        assertThatThrownBy(
                        () ->
                                deliveries.markFailed(
                                        eventId, acquired.claimToken(), "again", NOW, false))
                .isInstanceOf(StaleOrderConfirmationClaimException.class);
        assertThat(row(eventId)).isEqualTo(completed).containsEntry("status", outcome);
    }

    @Test
    void firstClaimPersistsTheRequestedLeaseWithoutRecordingAFailure() {
        UUID eventId = UUID.randomUUID();

        assertThat(claim(eventId))
                .isInstanceOfSatisfying(
                        OrderConfirmationClaim.Acquired.class,
                        acquired -> assertThat(acquired.failedAttempts()).isEqualTo(0));

        assertThat(row(eventId))
                .containsEntry("order_number", ORDER_NUMBER)
                .containsEntry("status", "CLAIMED")
                .containsEntry("attempt_count", 0)
                .containsEntry("next_attempt_at", Timestamp.from(NOW))
                .containsEntry("claim_expires_at", Timestamp.from(NOW.plusSeconds(300)))
                .containsEntry("created_at", Timestamp.from(NOW))
                .containsEntry("updated_at", Timestamp.from(NOW));
    }

    @Test
    void liveLeaseIsUnavailableAndBecomesReclaimableExactlyAtExpiry() {
        UUID eventId = UUID.randomUUID();
        assertThat(claim(eventId)).isInstanceOf(OrderConfirmationClaim.Acquired.class);
        Map<String, Object> original = row(eventId);

        doReturn(NOW.plusSeconds(299)).when(clock).instant();
        assertThat(claim(eventId)).isInstanceOf(OrderConfirmationClaim.Pending.class);
        assertThat(row(eventId)).isEqualTo(original);

        doReturn(NOW.plusSeconds(300)).when(clock).instant();
        assertThat(claim(eventId))
                .isInstanceOfSatisfying(
                        OrderConfirmationClaim.Acquired.class,
                        acquired -> assertThat(acquired.failedAttempts()).isEqualTo(0));
        assertThat(row(eventId))
                .containsEntry("claim_expires_at", Timestamp.from(NOW.plusSeconds(600)))
                .containsEntry("created_at", Timestamp.from(NOW))
                .containsEntry("updated_at", Timestamp.from(NOW.plusSeconds(300)));
    }

    @Test
    void futureRetryRemainsUnavailableEvenAfterThePreviousLeaseExpires() {
        UUID eventId = UUID.randomUUID();
        assertThat(claim(eventId)).isInstanceOf(OrderConfirmationClaim.Acquired.class);
        deliveries.markFailed(eventId, token(eventId), "unavailable", NOW.plusSeconds(600), false);
        Map<String, Object> original = row(eventId);

        doReturn(NOW.plusSeconds(599)).when(clock).instant();

        assertThat(claim(eventId)).isInstanceOf(OrderConfirmationClaim.Pending.class);
        assertThat(row(eventId)).isEqualTo(original);
    }

    @Test
    void retryBecomesAvailableExactlyWhenDueAndPreservesPriorFailures() {
        UUID eventId = UUID.randomUUID();
        assertThat(claim(eventId)).isInstanceOf(OrderConfirmationClaim.Acquired.class);
        deliveries.markFailed(eventId, token(eventId), "first failure", NOW.plusSeconds(1), false);
        doReturn(NOW.plusSeconds(1)).when(clock).instant();
        assertThat(claim(eventId))
                .isInstanceOfSatisfying(
                        OrderConfirmationClaim.Acquired.class,
                        acquired -> assertThat(acquired.failedAttempts()).isEqualTo(1));
        deliveries.markFailed(eventId, token(eventId), "second failure", NOW.plusSeconds(3), false);
        doReturn(NOW.plusSeconds(3)).when(clock).instant();

        assertThat(claim(eventId))
                .isInstanceOfSatisfying(
                        OrderConfirmationClaim.Acquired.class,
                        acquired -> assertThat(acquired.failedAttempts()).isEqualTo(2));

        assertThat(row(eventId))
                .containsEntry("status", "CLAIMED")
                .containsEntry("attempt_count", 2)
                .containsEntry("last_error", "second failure")
                .containsEntry("next_attempt_at", Timestamp.from(NOW.plusSeconds(3)))
                .containsEntry("claim_expires_at", Timestamp.from(NOW.plusSeconds(303)))
                .containsEntry("created_at", Timestamp.from(NOW));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SENT", "QUARANTINED"})
    void terminalDeliveryCannotBeClaimedAfterItsLeaseAndRetryTime(String status) {
        UUID eventId = UUID.randomUUID();
        assertThat(claim(eventId)).isInstanceOf(OrderConfirmationClaim.Acquired.class);
        if (status.equals("SENT")) {
            deliveries.markSent(eventId, token(eventId), NOW);
        } else {
            deliveries.markFailed(eventId, token(eventId), "permanent failure", NOW, true);
        }
        Map<String, Object> original = row(eventId);
        assertThat(original).containsEntry("status", status);
        doReturn(NOW.plusSeconds(3600)).when(clock).instant();

        assertThat(claim(eventId)).isEqualTo(OrderConfirmationClaim.Terminal.valueOf(status));
        assertThat(row(eventId)).isEqualTo(original);
    }

    @Test
    void fifthWorkflowFailurePersistsQuarantineAndKeepsTheOriginalFailure() {
        UUID eventId = UUID.randomUUID();
        var failure = new IllegalStateException("x".repeat(300));
        var service =
                new OrderConfirmationDeliveryService(
                        deliveries,
                        event -> {
                            throw failure;
                        },
                        confirmation -> {},
                        clock);
        var event = event(eventId);
        long[] attemptSeconds = {0, 1, 3, 7, 15};
        long[] nextAttemptSeconds = {1, 3, 7, 15, 15};
        for (int attempt = 0; attempt < attemptSeconds.length; attempt++) {
            doReturn(NOW.plusSeconds(attemptSeconds[attempt])).when(clock).instant();

            assertThatThrownBy(() -> service.send(event)).isSameAs(failure);

            assertThat(row(eventId))
                    .containsEntry("status", attempt == 4 ? "QUARANTINED" : "FAILED")
                    .containsEntry("attempt_count", attempt + 1)
                    .containsEntry(
                            "next_attempt_at",
                            Timestamp.from(NOW.plusSeconds(nextAttemptSeconds[attempt])));
        }
        assertThat(row(eventId).get("last_error").toString()).hasSize(200);
        doReturn(NOW.plusSeconds(3600)).when(clock).instant();
        assertThat(claim(eventId)).isEqualTo(OrderConfirmationClaim.Terminal.QUARANTINED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"NEW", "FAILED", "CLAIMED"})
    void simultaneousTransactionsGrantOneLeaseAndRecoverItAfterACrash(String initialStatus)
            throws Exception {
        UUID eventId = UUID.randomUUID();
        final int failedAttempts = initialStatus.equals("FAILED") ? 1 : 0;
        if (!initialStatus.equals("NEW")) {
            assertThat(claim(eventId)).isInstanceOf(OrderConfirmationClaim.Acquired.class);
            if (initialStatus.equals("FAILED")) {
                deliveries.markFailed(eventId, token(eventId), "previous failure", NOW, false);
            } else {
                doReturn(NOW.plusSeconds(300)).when(clock).instant();
            }
        }
        Instant claimedAt = clock.instant();
        var barrier = new CyclicBarrier(2);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var first = pool.submit(() -> concurrentClaim(eventId, barrier));
            var second = pool.submit(() -> concurrentClaim(eventId, barrier));

            assertThat(List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS)))
                    .filteredOn(OrderConfirmationClaim.Acquired.class::isInstance)
                    .hasSize(1);

            // Neither caller records an outcome: the winning process is assumed to have crashed.
            doReturn(claimedAt.plusSeconds(299)).when(clock).instant();
            assertThat(pool.submit(() -> claim(eventId)).get(10, TimeUnit.SECONDS))
                    .isInstanceOf(OrderConfirmationClaim.Pending.class);
            doReturn(claimedAt.plusSeconds(301)).when(clock).instant();
            assertThat(pool.submit(() -> claim(eventId)).get(10, TimeUnit.SECONDS))
                    .isInstanceOfSatisfying(
                            OrderConfirmationClaim.Acquired.class,
                            acquired ->
                                    assertThat(acquired.failedAttempts())
                                            .isEqualTo(failedAttempts));
            assertThat(claim(eventId)).isInstanceOf(OrderConfirmationClaim.Pending.class);
            assertThat(row(eventId))
                    .containsEntry("attempt_count", failedAttempts)
                    .containsEntry("claim_expires_at", Timestamp.from(claimedAt.plusSeconds(601)));
        } finally {
            pool.shutdownNow();
        }
    }

    private OrderConfirmationClaim concurrentClaim(UUID eventId, CyclicBarrier barrier) {
        return new TransactionTemplate(transactions)
                .execute(
                        status -> {
                            try {
                                barrier.await(5, TimeUnit.SECONDS);
                            } catch (Exception exception) {
                                throw new IllegalStateException(
                                        "Could not synchronize claim transactions", exception);
                            }
                            return claim(eventId);
                        });
    }

    private OrderConfirmationClaim claim(UUID eventId) {
        Instant now = clock.instant();
        return deliveries.claim(
                eventId, ORDER_NUMBER, UUID.randomUUID(), now, now.plusSeconds(300));
    }

    private UUID token(UUID eventId) {
        return (UUID) row(eventId).get("claim_token");
    }

    private Map<String, Object> row(UUID eventId) {
        return jdbc.queryForMap(
                "select * from order_confirmation_delivery where event_id = ?", eventId);
    }

    private OrderPlacedIntegrationEvent event(UUID eventId) {
        var address =
                new OrderPlacedIntegrationEvent.Address(
                        "Ada", null, "1 Main", null, "Berlin", null, "10115", "DE", null);
        return new OrderPlacedIntegrationEvent(
                eventId,
                UUID.randomUUID(),
                ORDER_NUMBER,
                42L,
                "Ada",
                "ada@example.com",
                NOW,
                new BigDecimal("39.98"),
                Currency.EUR,
                List.of(),
                address,
                address);
    }
}
