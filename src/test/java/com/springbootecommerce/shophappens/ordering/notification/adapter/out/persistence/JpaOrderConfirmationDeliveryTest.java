package com.springbootecommerce.shophappens.ordering.notification.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationClaim;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JpaOrderConfirmationDeliveryTest {
    private static final UUID EVENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String ORDER_NUMBER = "ORD-2026-100001";
    private static final Instant NOW = Instant.parse("2026-09-13T10:00:00Z");
    private static final Instant EXPIRES_AT = NOW.plusSeconds(300);

    @Mock SpringDataOrderConfirmationDeliveryRepository repository;
    JpaOrderConfirmationDelivery delivery;

    @BeforeEach
    void setUp() {
        delivery = new JpaOrderConfirmationDelivery(repository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void firstClaimHasNoPriorFailures() {
        when(repository.claim(EVENT_ID, ORDER_NUMBER, NOW, EXPIRES_AT)).thenReturn(Optional.of(0));

        assertThat(delivery.claim(EVENT_ID, ORDER_NUMBER, NOW, EXPIRES_AT))
                .contains(new OrderConfirmationClaim(0));
    }

    @Test
    void dueRetryReturnsItsPriorFailures() {
        when(repository.claim(EVENT_ID, ORDER_NUMBER, NOW, EXPIRES_AT)).thenReturn(Optional.of(3));

        assertThat(delivery.claim(EVENT_ID, ORDER_NUMBER, NOW, EXPIRES_AT))
                .contains(new OrderConfirmationClaim(3));
    }

    @Test
    void ineligibleEventReturnsNoClaim() {
        when(repository.claim(EVENT_ID, ORDER_NUMBER, NOW, EXPIRES_AT))
                .thenReturn(Optional.empty());

        assertThat(delivery.claim(EVENT_ID, ORDER_NUMBER, NOW, EXPIRES_AT)).isEmpty();
    }

    @Test
    void recordsSentState() {
        var entity = entity();
        when(repository.findById(EVENT_ID)).thenReturn(Optional.of(entity));

        delivery.markSent(EVENT_ID, NOW.minusSeconds(1));

        assertThat(entity.getStatus()).isEqualTo("SENT");
        assertThat(entity.getSentAt()).isEqualTo(NOW.minusSeconds(1));
        assertThat(entity.getUpdatedAt()).isEqualTo(NOW);
        verify(repository).save(entity);
    }

    @Test
    void recordsRetryAndBoundsItsDiagnostic() {
        var entity = entity();
        entity.setAttemptCount(2);
        when(repository.findById(EVENT_ID)).thenReturn(Optional.of(entity));

        delivery.markFailed(EVENT_ID, "x".repeat(300), NOW.plusSeconds(4), false);

        assertThat(entity.getStatus()).isEqualTo("FAILED");
        assertThat(entity.getAttemptCount()).isEqualTo(3);
        assertThat(entity.getLastError()).hasSize(200);
        assertThat(entity.getNextAttemptAt()).isEqualTo(NOW.plusSeconds(4));
        assertThat(entity.getUpdatedAt()).isEqualTo(NOW);
        verify(repository).save(entity);
    }

    @Test
    void honorsQuarantineRequestedByTheWorkflow() {
        var entity = entity();
        when(repository.findById(EVENT_ID)).thenReturn(Optional.of(entity));

        delivery.markFailed(EVENT_ID, null, NOW, true);

        assertThat(entity.getStatus()).isEqualTo("QUARANTINED");
        assertThat(entity.getAttemptCount()).isEqualTo(1);
        assertThat(entity.getLastError()).isNull();
        assertThat(entity.getNextAttemptAt()).isEqualTo(NOW);
        verify(repository).save(entity);
    }

    private OrderConfirmationDeliveryJpaEntity entity() {
        var entity = new OrderConfirmationDeliveryJpaEntity();
        entity.setEventId(EVENT_ID);
        entity.setOrderNumber(ORDER_NUMBER);
        entity.setStatus("CLAIMED");
        entity.setNextAttemptAt(NOW);
        entity.setClaimExpiresAt(EXPIRES_AT);
        entity.setCreatedAt(NOW);
        entity.setUpdatedAt(NOW);
        return entity;
    }
}
