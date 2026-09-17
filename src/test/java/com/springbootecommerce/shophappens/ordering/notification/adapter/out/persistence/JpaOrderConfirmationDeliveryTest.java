package com.springbootecommerce.shophappens.ordering.notification.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JpaOrderConfirmationDeliveryTest {
    @Mock SpringDataOrderConfirmationDeliveryRepository repository;

    private static final UUID EVENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final Instant NOW = Instant.parse("2026-09-13T10:00:00Z");

    @Test
    void claimsAnEventOnlyOnce() {
        var delivery =
                new JpaOrderConfirmationDelivery(repository, Clock.fixed(NOW, ZoneOffset.UTC));
        var entity = OrderConfirmationDeliveryJpaEntity.create(EVENT_ID, "ORD-2026-100001", NOW);
        when(repository.findById(EVENT_ID)).thenReturn(Optional.empty(), Optional.of(entity));

        assertThat(delivery.claim(EVENT_ID, "ORD-2026-100001")).isTrue();
        assertThat(delivery.claim(EVENT_ID, "ORD-2026-100001")).isFalse();

        verify(repository).save(any(OrderConfirmationDeliveryJpaEntity.class));
    }

    @Test
    void recordsSentStateAndBoundedFailureDiagnostics() {
        var entity = OrderConfirmationDeliveryJpaEntity.create(EVENT_ID, "ORD-2026-100001", NOW);
        when(repository.findById(EVENT_ID)).thenReturn(Optional.of(entity));
        var delivery =
                new JpaOrderConfirmationDelivery(repository, Clock.fixed(NOW, ZoneOffset.UTC));

        delivery.markSent(EVENT_ID, NOW);
        assertThat(entity.getStatus()).isEqualTo("SENT");
        assertThat(entity.getSentAt()).isEqualTo(NOW);
        delivery.markFailed(EVENT_ID, "x".repeat(300), NOW.plusSeconds(4));

        assertThat(entity.getStatus()).isEqualTo("FAILED");
        assertThat(entity.getLastError()).hasSize(200);
        assertThat(entity.getNextAttemptAt()).isEqualTo(NOW.plusSeconds(4));
        verify(repository, org.mockito.Mockito.times(2)).save(entity);
    }

    @Test
    void doesNotReclaimQuarantinedEvent() {
        var entity = OrderConfirmationDeliveryJpaEntity.create(EVENT_ID, "ORD-2026-100001", NOW);
        entity.setStatus("QUARANTINED");
        when(repository.findById(EVENT_ID)).thenReturn(Optional.of(entity));
        var delivery =
                new JpaOrderConfirmationDelivery(repository, Clock.fixed(NOW, ZoneOffset.UTC));

        assertThat(delivery.claim(EVENT_ID, "ORD-2026-100001")).isFalse();
    }
}
