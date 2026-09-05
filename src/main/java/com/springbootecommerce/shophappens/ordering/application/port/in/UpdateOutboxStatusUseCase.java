package com.springbootecommerce.shophappens.ordering.application.port.in;

import java.time.Instant;
import java.util.UUID;

public interface UpdateOutboxStatusUseCase {
    void markPublished(UUID eventId, Instant publishedAt);

    void markFailed(UUID eventId, String error);
}
