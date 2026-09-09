package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import com.springbootecommerce.shophappens.ordering.application.port.out.IntegrationEventOutbox;
import com.springbootecommerce.shophappens.ordering.application.port.out.IntegrationEventOutbox.PendingIntegrationEvent;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

class OutboxRetryIT extends AbstractIntegrationTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired IntegrationEventOutbox outbox;
    @MockitoSpyBean Clock clock;

    @BeforeEach
    void clearOutbox() {
        jdbc.execute("truncate table integration_outbox");
    }

    @Test
    void failedFirstBatchAllowsLaterEligibleEventsToProgress() {
        Instant now = Instant.parse("2026-09-07T10:00:00Z");
        doReturn(now).when(clock).instant();
        for (int i = 0; i < 100; i++) {
            insert(UUID.randomUUID(), now.minusSeconds(60));
        }
        UUID healthy = UUID.randomUUID();
        insert(healthy, now.minusSeconds(30));

        var first = outbox.pending(100);
        assertThat(first).hasSize(100);
        first.forEach(event -> outbox.markFailed(event.eventId(), "Broker unavailable"));

        assertThat(outbox.pending(100))
                .extracting(PendingIntegrationEvent::eventId)
                .containsExactly(healthy);
    }

    @Test
    void eventBecomesEligibleExactlyAtItsRetryTimeAndPublishedRowsStayHidden() {
        Instant now = Instant.parse("2026-09-07T11:00:00Z");
        Instant due = now.plusSeconds(10);
        UUID eventId = UUID.randomUUID();
        insert(eventId, now.minusSeconds(30), due, "{\"kind\":\"due\"}");

        doReturn(now).when(clock).instant();
        assertThat(outbox.pending(10)).isEmpty();

        doReturn(due).when(clock).instant();
        assertThat(outbox.pending(10))
                .extracting(PendingIntegrationEvent::eventId)
                .containsExactly(eventId);

        outbox.markPublished(eventId, due);
        outbox.markFailed(eventId, "must be ignored after publication");
        assertThat(outbox.pending(10)).isEmpty();
        assertThat(
                        jdbc.queryForObject(
                                "select attempt_count from integration_outbox where event_id = ?",
                                Integer.class,
                                eventId))
                .isZero();
    }

    @Test
    void fifthFailureQuarantinesAndBoundsTheStoredDiagnostic() {
        Instant now = Instant.parse("2026-09-07T12:00:00Z");
        doReturn(now).when(clock).instant();
        UUID eventId = UUID.randomUUID();
        insert(eventId, now.minusSeconds(1));

        Instant attemptTime = now;
        for (int attempt = 0; attempt < 4; attempt++) {
            doReturn(attemptTime).when(clock).instant();
            outbox.markFailed(eventId, "x".repeat(250));
            Instant expectedNextAttempt = attemptTime.plusSeconds(1L << attempt);
            assertThat(
                            jdbc.queryForObject(
                                            "select next_attempt_at from integration_outbox where event_id = ?",
                                            Timestamp.class,
                                            eventId)
                                    .toInstant())
                    .isEqualTo(expectedNextAttempt);
            attemptTime = expectedNextAttempt;
        }
        doReturn(attemptTime).when(clock).instant();
        outbox.markFailed(eventId, "x".repeat(250));
        outbox.markFailed(eventId, "must be ignored after quarantine");

        Map<String, Object> row =
                jdbc.queryForMap(
                        """
                        select attempt_count, last_error, quarantined_at
                        from integration_outbox where event_id = ?
                        """,
                        eventId);
        assertThat(row.get("attempt_count")).isEqualTo(5);
        assertThat(row.get("last_error").toString()).hasSize(200);
        assertThat(((Timestamp) row.get("quarantined_at")).toInstant()).isEqualTo(attemptTime);

        doReturn(now.plusSeconds(3600)).when(clock).instant();
        assertThat(outbox.pending(10)).isEmpty();
    }

    @Test
    void operatorCanReplayOneQuarantinedEventWithoutChangingIdentityOrPayload() {
        Instant now = Instant.parse("2099-09-07T12:00:00Z");
        doReturn(now).when(clock).instant();
        UUID eventId = UUID.randomUUID();
        String payload = "{\"eventId\":\"" + eventId + "\"}";
        insert(eventId, now.minusSeconds(1), now.minusSeconds(1), payload);
        for (int attempt = 0; attempt < 5; attempt++) {
            outbox.markFailed(eventId, "Broker unavailable");
        }

        int affected =
                jdbc.update(
                        """
                        UPDATE integration_outbox
                        SET quarantined_at = NULL, attempt_count = 0,
                            next_attempt_at = CURRENT_TIMESTAMP, last_error = NULL
                        WHERE event_id = ? AND published_at IS NULL
                          AND quarantined_at IS NOT NULL
                        """,
                        eventId);
        assertThat(affected).isEqualTo(1);
        doReturn(Instant.parse("2100-01-01T00:00:00Z")).when(clock).instant();

        assertThat(outbox.pending(10))
                .singleElement()
                .satisfies(
                        replayed -> {
                            assertThat(replayed.eventId()).isEqualTo(eventId);
                            assertThat(replayed.payload()).isEqualTo(payload);
                        });
    }

    private void insert(UUID id, Instant created) {
        insert(id, created, created, "{}");
    }

    private void insert(UUID id, Instant created, Instant nextAttempt, String payload) {
        jdbc.update(
                """
                insert into integration_outbox
                  (event_id,event_type,aggregate_type,aggregate_key,payload,created_at,next_attempt_at)
                values (?,'ordering.order-placed.v1','Order',?,?,?,?)
                """,
                id,
                UUID.randomUUID().toString(),
                payload,
                Timestamp.from(created),
                Timestamp.from(nextAttempt));
    }
}
