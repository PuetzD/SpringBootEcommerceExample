package com.springbootecommerce.shophappens.ordering.application.service;

import com.springbootecommerce.shophappens.ordering.application.port.in.UpdateOutboxStatusUseCase;
import com.springbootecommerce.shophappens.ordering.application.port.out.IntegrationEventOutbox;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxStatusService implements UpdateOutboxStatusUseCase {
    private final IntegrationEventOutbox outbox;

    @Transactional
    public void markPublished(UUID eventId, Instant publishedAt) {
        outbox.markPublished(eventId, publishedAt);
    }

    @Transactional
    public void markFailed(UUID eventId, String error) {
        outbox.markFailed(eventId, error);
    }
}
