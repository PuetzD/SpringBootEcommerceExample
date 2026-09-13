package com.springbootecommerce.shophappens.ordering.notification.adapter.out.persistence;

import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationDelivery;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class JpaOrderConfirmationDelivery implements OrderConfirmationDelivery {
    private final SpringDataOrderConfirmationDeliveryRepository repository;
    private final Clock clock;

    @Override
    @Transactional
    public boolean claim(UUID eventId, String orderNumber) {
        var existing = repository.findById(eventId);
        if (existing.isPresent()) {
            var entity = existing.get();
            if (entity.getStatus().equals("SENT")
                    || entity.getStatus().equals("CLAIMED")
                    || entity.getStatus().equals("QUARANTINED")) {
                return false;
            }
            entity.setStatus("CLAIMED");
            entity.setUpdatedAt(Instant.now(clock));
            repository.save(entity);
            return true;
        }
        try {
            repository.save(
                    OrderConfirmationDeliveryJpaEntity.create(
                            eventId, orderNumber, Instant.now(clock)));
            return true;
        } catch (DataIntegrityViolationException exception) {
            return false;
        }
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
    public void markFailed(UUID eventId, String diagnostic, Instant nextAttemptAt) {
        repository
                .findById(eventId)
                .ifPresent(
                        entity -> {
                            entity.setStatus("FAILED");
                            entity.setAttemptCount(entity.getAttemptCount() + 1);
                            if (entity.getAttemptCount() >= 5) entity.setStatus("QUARANTINED");
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
