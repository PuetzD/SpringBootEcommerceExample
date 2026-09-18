package com.springbootecommerce.shophappens.ordering.notification.adapter.out.persistence;

import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationClaim;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationDelivery;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class JpaOrderConfirmationDelivery implements OrderConfirmationDelivery {
    private final SpringDataOrderConfirmationDeliveryRepository repository;
    private final Clock clock;

    @Override
    @Transactional
    public Optional<OrderConfirmationClaim> claim(
            UUID eventId, String orderNumber, Instant now, Instant claimExpiresAt) {
        return repository
                .claim(eventId, orderNumber, now, claimExpiresAt)
                .map(OrderConfirmationClaim::new);
    }

    @Override
    @Transactional
    public void markSent(UUID eventId, Instant sentAt) {
        repository
                .findById(eventId)
                .ifPresent(
                        entity -> {
                            entity.setStatus("SENT");
                            entity.setSentAt(sentAt);
                            entity.setUpdatedAt(Instant.now(clock));
                            repository.save(entity);
                        });
    }

    @Override
    @Transactional
    public void markFailed(
            UUID eventId, String diagnostic, Instant nextAttemptAt, boolean quarantine) {
        repository
                .findById(eventId)
                .ifPresent(
                        entity -> {
                            entity.setStatus(quarantine ? "QUARANTINED" : "FAILED");
                            entity.setAttemptCount(entity.getAttemptCount() + 1);
                            entity.setLastError(
                                    diagnostic == null
                                            ? null
                                            : diagnostic.substring(
                                                    0, Math.min(200, diagnostic.length())));
                            entity.setNextAttemptAt(nextAttemptAt);
                            entity.setUpdatedAt(Instant.now(clock));
                            repository.save(entity);
                        });
    }
}
