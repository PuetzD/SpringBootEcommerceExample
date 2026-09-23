package com.springbootecommerce.shophappens.ordering.notification.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationClaim;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.StaleOrderConfirmationClaimException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JpaOrderConfirmationDeliveryTest {
    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID TOKEN = UUID.randomUUID();
    private static final String ORDER_NUMBER = "ORD-2026-100001";
    private static final Instant NOW = Instant.parse("2026-09-13T10:00:00Z");
    private static final Instant EXPIRES_AT = NOW.plusSeconds(300);
    @Mock SpringDataOrderConfirmationDeliveryRepository repository;
    JpaOrderConfirmationDelivery delivery;

    @BeforeEach
    void setUp() {
        delivery = new JpaOrderConfirmationDelivery(repository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 3})
    void acquiredClaimReturnsPreviousFailuresAndTheCallerToken(int failures) {
        when(repository.claim(EVENT_ID, ORDER_NUMBER, TOKEN, NOW, EXPIRES_AT))
                .thenReturn(Optional.of(failures));
        assertThat(delivery.claim(EVENT_ID, ORDER_NUMBER, TOKEN, NOW, EXPIRES_AT))
                .isEqualTo(new OrderConfirmationClaim.Acquired(failures, TOKEN));
    }

    @ParameterizedTest
    @ValueSource(strings = {"FAILED", "CLAIMED"})
    void refusedClaimReturnsTheEligibilityOfItsCurrentState(String status) {
        var entity = new OrderConfirmationDeliveryJpaEntity();
        entity.setStatus(status);
        entity.setNextAttemptAt(NOW.plusSeconds(8));
        entity.setClaimExpiresAt(EXPIRES_AT);
        when(repository.findById(EVENT_ID)).thenReturn(Optional.of(entity));

        assertThat(delivery.claim(EVENT_ID, ORDER_NUMBER, TOKEN, NOW, EXPIRES_AT))
                .isEqualTo(
                        new OrderConfirmationClaim.Pending(
                                status.equals("FAILED") ? NOW.plusSeconds(8) : EXPIRES_AT));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SENT", "QUARANTINED"})
    void refusedTerminalClaimReturnsNormally(String status) {
        var entity = new OrderConfirmationDeliveryJpaEntity();
        entity.setStatus(status);
        when(repository.findById(EVENT_ID)).thenReturn(Optional.of(entity));
        assertThat(delivery.claim(EVENT_ID, ORDER_NUMBER, TOKEN, NOW, EXPIRES_AT))
                .isEqualTo(OrderConfirmationClaim.Terminal.valueOf(status));
    }

    @Test
    void recordsSentWithTheOwnerToken() {
        when(repository.markSent(EVENT_ID, TOKEN, NOW.minusSeconds(1), NOW)).thenReturn(1);
        delivery.markSent(EVENT_ID, TOKEN, NOW.minusSeconds(1));
        verify(repository).markSent(EVENT_ID, TOKEN, NOW.minusSeconds(1), NOW);
    }

    @Test
    void boundsDiagnosticBeforeTheAtomicFailureUpdate() {
        when(repository.markFailed(
                        EVENT_ID, TOKEN, "FAILED", "x".repeat(200), NOW.plusSeconds(4), NOW))
                .thenReturn(1);
        delivery.markFailed(EVENT_ID, TOKEN, "x".repeat(300), NOW.plusSeconds(4), false);
        verify(repository)
                .markFailed(EVENT_ID, TOKEN, "FAILED", "x".repeat(200), NOW.plusSeconds(4), NOW);
    }

    @Test
    void recordsQuarantineWithANullDiagnostic() {
        when(repository.markFailed(EVENT_ID, TOKEN, "QUARANTINED", null, NOW, NOW)).thenReturn(1);
        delivery.markFailed(EVENT_ID, TOKEN, null, NOW, true);
        verify(repository).markFailed(EVENT_ID, TOKEN, "QUARANTINED", null, NOW, NOW);
    }

    @Test
    void zeroUpdatedRowsRejectsStaleSuccess() {
        assertThatThrownBy(() -> delivery.markSent(EVENT_ID, TOKEN, NOW))
                .isInstanceOf(StaleOrderConfirmationClaimException.class);
    }

    @Test
    void zeroUpdatedRowsRejectsStaleFailure() {
        assertThatThrownBy(() -> delivery.markFailed(EVENT_ID, TOKEN, "failure", NOW, false))
                .isInstanceOf(StaleOrderConfirmationClaimException.class);
    }
}
