package com.springbootecommerce.shophappens.ordering.adapter.out.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.ordering.application.port.in.UpdateOutboxStatusUseCase;
import com.springbootecommerce.shophappens.ordering.application.port.out.IntegrationEventOutbox;
import com.springbootecommerce.shophappens.ordering.application.port.out.IntegrationEventOutbox.PendingIntegrationEvent;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class OutboxKafkaPublisherTest {
    @Mock IntegrationEventOutbox outbox;
    @Mock UpdateOutboxStatusUseCase statuses;
    @Mock KafkaTemplate<String, String> kafka;
    private static final Instant PUBLISHED_AT = Instant.parse("2026-08-31T10:15:32Z");

    @ParameterizedTest
    @CsvSource({"1", "2"})
    void publishesStoredOrderPlacedPayloadToItsVersionedTopic(String version) throws Exception {
        UUID eventId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        String type = "ordering.order-placed.v" + version;
        String storedPayload = "{\"eventId\":\"11111111-1111-1111-1111-111111111111\"}";
        PendingIntegrationEvent event =
                new PendingIntegrationEvent(
                        eventId, type, "22222222-2222-2222-2222-222222222222", storedPayload);
        when(outbox.pending(100)).thenReturn(List.of(event));
        when(kafka.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(null));
        OutboxKafkaPublisher publisher =
                new OutboxKafkaPublisher(
                        outbox, statuses, kafka, Clock.fixed(PUBLISHED_AT, ZoneOffset.UTC));

        publisher.publishPending();

        ArgumentCaptor<ProducerRecord<String, String>> record =
                ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafka).send(record.capture());
        assertThat(record.getValue().topic()).isEqualTo(type);
        assertThat(record.getValue().key()).isEqualTo("22222222-2222-2222-2222-222222222222");
        assertThat(record.getValue().value()).isEqualTo(storedPayload);
        assertThat(
                        new String(
                                record.getValue().headers().lastHeader("event-type").value(),
                                StandardCharsets.UTF_8))
                .isEqualTo(type);
        assertThat(
                        new String(
                                record.getValue().headers().lastHeader("event-version").value(),
                                StandardCharsets.UTF_8))
                .isEqualTo(version);
        assertThat(
                        new String(
                                record.getValue().headers().lastHeader("event-id").value(),
                                StandardCharsets.UTF_8))
                .isEqualTo("11111111-1111-1111-1111-111111111111");
        verify(statuses).markPublished(eventId, PUBLISHED_AT);
    }

    @Test
    void rejectsUnknownStoredEventTypesWithoutPublishingAnArbitraryTopic() {
        UUID eventId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        when(outbox.pending(100))
                .thenReturn(
                        List.of(
                                new PendingIntegrationEvent(
                                        eventId,
                                        "ordering.order-refunded.v1",
                                        "44444444-4444-4444-4444-444444444444",
                                        "{}")));
        OutboxKafkaPublisher publisher =
                new OutboxKafkaPublisher(
                        outbox, statuses, kafka, Clock.fixed(PUBLISHED_AT, ZoneOffset.UTC));

        publisher.publishPending();

        verifyNoInteractions(kafka);
        verify(statuses).markFailed(eventId, "IllegalArgumentException");
        verify(statuses, never()).markPublished(any(), any());
    }

    @Test
    void timeoutMarksFailureAndContinuesWithTheNextEvent() throws Exception {
        UUID timedOutId = UUID.fromString("55555555-5555-5555-5555-555555555555");
        UUID healthyId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        when(outbox.pending(100))
                .thenReturn(
                        List.of(pending(timedOutId, "timed-out"), pending(healthyId, "healthy")));
        CompletableFuture<SendResult<String, String>> timedOut = mock(CompletableFuture.class);
        when(timedOut.get(10, TimeUnit.SECONDS)).thenThrow(new TimeoutException());
        when(kafka.send(any(ProducerRecord.class)))
                .thenReturn(timedOut, CompletableFuture.completedFuture(null));
        OutboxKafkaPublisher publisher =
                new OutboxKafkaPublisher(
                        outbox, statuses, kafka, Clock.fixed(PUBLISHED_AT, ZoneOffset.UTC));

        publisher.publishPending();

        verify(statuses).markFailed(timedOutId, "TimeoutException");
        verify(statuses, never()).markPublished(timedOutId, PUBLISHED_AT);
        verify(statuses).markPublished(healthyId, PUBLISHED_AT);
        verify(kafka, times(2)).send(any(ProducerRecord.class));
    }

    @Test
    void interruptionStopsTheBatchAndRestoresTheThreadFlag() throws Exception {
        UUID interruptedId = UUID.fromString("77777777-7777-7777-7777-777777777777");
        UUID untouchedId = UUID.fromString("88888888-8888-8888-8888-888888888888");
        when(outbox.pending(100))
                .thenReturn(
                        List.of(
                                pending(interruptedId, "interrupted"),
                                pending(untouchedId, "untouched")));
        CompletableFuture<SendResult<String, String>> interrupted = mock(CompletableFuture.class);
        when(interrupted.get(10, TimeUnit.SECONDS)).thenThrow(new InterruptedException());
        when(kafka.send(any(ProducerRecord.class))).thenReturn(interrupted);
        OutboxKafkaPublisher publisher =
                new OutboxKafkaPublisher(
                        outbox, statuses, kafka, Clock.fixed(PUBLISHED_AT, ZoneOffset.UTC));

        try {
            publisher.publishPending();

            assertThat(Thread.currentThread().isInterrupted()).isTrue();
            verify(kafka).send(any(ProducerRecord.class));
            verifyNoInteractions(statuses);
        } finally {
            Thread.interrupted();
        }
    }

    private static PendingIntegrationEvent pending(UUID eventId, String aggregateKey) {
        return new PendingIntegrationEvent(eventId, "ordering.order-placed.v2", aggregateKey, "{}");
    }
}
