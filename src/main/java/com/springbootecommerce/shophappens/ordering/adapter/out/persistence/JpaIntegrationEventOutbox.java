package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import com.springbootecommerce.shophappens.ordering.application.event.OrderPlacedIntegrationEvent;
import com.springbootecommerce.shophappens.ordering.application.port.out.IntegrationEventOutbox;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Repository
@RequiredArgsConstructor
class JpaIntegrationEventOutbox implements IntegrationEventOutbox {
    private final SpringDataOutboxRepository repository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    @Transactional
    public void append(OrderPlacedIntegrationEvent event) {
        try {
            repository.save(
                    OutboxEventJpaEntity.create(
                            event.eventId(),
                            OrderPlacedIntegrationEvent.EVENT_TYPE,
                            event.orderId().toString(),
                            objectMapper.writeValueAsString(event),
                            Instant.now(clock)));
        } catch (JacksonException exception) {
            throw new IllegalStateException("Could not serialize order placed event", exception);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PendingIntegrationEvent> pending(int batchSize) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("batchSize must be positive");
        }
        return repository.findEligible(Instant.now(clock), PageRequest.of(0, batchSize)).stream()
                .map(
                        event ->
                                new PendingIntegrationEvent(
                                        event.getEventId(),
                                        event.getEventType(),
                                        event.getAggregateKey(),
                                        event.getPayload()))
                .toList();
    }

    @Override
    @Transactional
    public void markPublished(UUID eventId, Instant publishedAt) {
        repository
                .findById(eventId)
                .ifPresent(
                        event -> {
                            event.setPublishedAt(publishedAt);
                            event.setLastError(null);
                            repository.save(event);
                        });
    }

    @Override
    @Transactional
    public void markFailed(UUID eventId, String error) {
        repository
                .findById(eventId)
                .ifPresent(
                        event -> {
                            if (event.getPublishedAt() != null
                                    || event.getQuarantinedAt() != null) {
                                return;
                            }
                            Instant now = Instant.now(clock);
                            int attempts = event.getAttemptCount() + 1;
                            event.setAttemptCount(attempts);
                            String diagnostic = error == null ? "Delivery failed" : error;
                            event.setLastError(
                                    diagnostic.substring(0, Math.min(200, diagnostic.length())));
                            if (attempts >= 5) {
                                event.setQuarantinedAt(now);
                            } else {
                                event.setNextAttemptAt(
                                        now.plusSeconds(
                                                Math.min(60, 1L << Math.min(attempts - 1, 6))));
                            }
                            repository.save(event);
                        });
    }
}
