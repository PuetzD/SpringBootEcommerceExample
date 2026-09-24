package com.springbootecommerce.shophappens.ordering.notification.adapter.out.persistence;

import com.springbootecommerce.shophappens.ordering.notification.application.port.in.OrderConfirmationDeliveryView;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationClaim;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationDelivery;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationDeliveryQuery;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.StaleOrderConfirmationClaimException;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class JpaOrderConfirmationDelivery
        implements OrderConfirmationDelivery, OrderConfirmationDeliveryQuery {
    private final SpringDataOrderConfirmationDeliveryRepository repository;
    private final Clock clock;

    @Override
    @Transactional
    public OrderConfirmationClaim claim(
            UUID eventId,
            String orderNumber,
            UUID claimToken,
            Instant now,
            Instant claimExpiresAt) {
        var acquired = repository.claim(eventId, orderNumber, claimToken, now, claimExpiresAt);
        if (acquired.isPresent()) {
            return new OrderConfirmationClaim.Acquired(acquired.orElseThrow(), claimToken);
        }
        // This read classifies a refused atomic claim; it never authorizes an outcome write.
        var delivery = repository.findById(eventId).orElseThrow();
        return switch (delivery.getStatus()) {
            case "FAILED" -> new OrderConfirmationClaim.Pending(delivery.getNextAttemptAt());
            case "CLAIMED" -> new OrderConfirmationClaim.Pending(delivery.getClaimExpiresAt());
            case "SENT" -> OrderConfirmationClaim.Terminal.SENT;
            case "QUARANTINED" -> OrderConfirmationClaim.Terminal.QUARANTINED;
            default -> throw new IllegalStateException("Unknown confirmation delivery status");
        };
    }

    @Override
    @Transactional
    public void markSent(UUID eventId, UUID claimToken, Instant sentAt) {
        requireOwner(eventId, repository.markSent(eventId, claimToken, sentAt, clock.instant()));
    }

    @Override
    @Transactional
    public void markFailed(
            UUID eventId,
            UUID claimToken,
            String diagnostic,
            Instant nextAttemptAt,
            boolean quarantine) {
        String boundedDiagnostic =
                diagnostic == null
                        ? null
                        : diagnostic.substring(0, Math.min(200, diagnostic.length()));
        requireOwner(
                eventId,
                repository.markFailed(
                        eventId,
                        claimToken,
                        quarantine ? "QUARANTINED" : "FAILED",
                        boundedDiagnostic,
                        nextAttemptAt,
                        clock.instant()));
    }

    private static void requireOwner(UUID eventId, int updated) {
        if (updated == 0) {
            throw new StaleOrderConfirmationClaimException(eventId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderConfirmationDeliveryView> findByOrderNumber(String orderNumber) {
        return repository.findByOrderNumber(orderNumber).map(this::toView);
    }

    private OrderConfirmationDeliveryView toView(
            OrderConfirmationDeliveryJpaEntity orderConfirmationDeliveryJpaEntity) {
        return new OrderConfirmationDeliveryView(
                orderConfirmationDeliveryJpaEntity.getStatus(),
                orderConfirmationDeliveryJpaEntity.getAttemptCount(),
                orderConfirmationDeliveryJpaEntity.getLastError(),
                orderConfirmationDeliveryJpaEntity.getNextAttemptAt(),
                orderConfirmationDeliveryJpaEntity.getSentAt());
    }
}
