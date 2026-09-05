package com.springbootecommerce.shophappens.ordering.adapter.out.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class OutboxKafkaPublisherTest {
    @Mock IntegrationEventOutbox outbox;
    @Mock UpdateOutboxStatusUseCase statuses;
    @Mock KafkaTemplate<String, String> kafka;
    private static final Instant PUBLISHED_AT = Instant.parse("2026-08-31T10:15:32Z");

    @Test
    void publishesTheOrderPlacedEventWithItsEstablishedTopicAndHeaders() throws Exception {
        UUID eventId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        PendingIntegrationEvent event =
                new PendingIntegrationEvent(
                        eventId,
                        "ordering.order-placed.v1",
                        "22222222-2222-2222-2222-222222222222",
                        "{\"eventId\":\"11111111-1111-1111-1111-111111111111\"}");
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
        assertThat(record.getValue().topic()).isEqualTo("ordering.order-placed.v1");
        assertThat(record.getValue().key()).isEqualTo("22222222-2222-2222-2222-222222222222");
        assertThat(record.getValue().value())
                .isEqualTo("{\"eventId\":\"11111111-1111-1111-1111-111111111111\"}");
        assertThat(
                        new String(
                                record.getValue().headers().lastHeader("event-type").value(),
                                StandardCharsets.UTF_8))
                .isEqualTo("ordering.order-placed.v1");
        assertThat(
                        new String(
                                record.getValue().headers().lastHeader("event-version").value(),
                                StandardCharsets.UTF_8))
                .isEqualTo("1");
        assertThat(
                        new String(
                                record.getValue().headers().lastHeader("event-id").value(),
                                StandardCharsets.UTF_8))
                .isEqualTo("11111111-1111-1111-1111-111111111111");
        verify(statuses).markPublished(eventId, PUBLISHED_AT);
    }
}
