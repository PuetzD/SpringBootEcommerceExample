package com.springbootecommerce.shophappens.ordering.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.ordering.application.event.OrderPlacedIntegrationEvent;
import com.springbootecommerce.shophappens.ordering.notification.application.port.in.OrderConfirmationPendingException;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationClaim;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationDelivery;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationRenderer;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationSender;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.RenderedOrderConfirmation;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.StaleOrderConfirmationClaimException;
import com.springbootecommerce.shophappens.sharedkernel.money.Currency;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderConfirmationDeliveryServiceTest {
    private static final UUID EVENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CLAIM_TOKEN = UUID.randomUUID();
    private static final String ORDER_NUMBER = "ORD-2026-100001";
    private static final Instant NOW = Instant.parse("2026-09-13T10:00:00Z");
    private static final RenderedOrderConfirmation RENDERED =
            new RenderedOrderConfirmation(
                    "shop@example.com", "ada@example.com", "Order placed", "text", "<p>html</p>");

    @Mock OrderConfirmationDelivery deliveries;
    @Mock OrderConfirmationRenderer renderer;
    @Mock OrderConfirmationSender sender;
    OrderConfirmationDeliveryService service;

    @BeforeEach
    void setUp() {
        service =
                new OrderConfirmationDeliveryService(
                        deliveries, renderer, sender, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void claimsForFiveMinutesThenSendsAndMarksTheDeliverySent() {
        var event = event();
        when(deliveries.claim(
                        eq(EVENT_ID),
                        eq(ORDER_NUMBER),
                        any(UUID.class),
                        eq(NOW),
                        eq(NOW.plusSeconds(300))))
                .thenReturn(new OrderConfirmationClaim.Acquired(0, CLAIM_TOKEN));
        when(renderer.render(event)).thenReturn(RENDERED);

        service.send(event);

        verify(sender).send(RENDERED);
        verify(deliveries).markSent(EVENT_ID, CLAIM_TOKEN, NOW);
    }

    @ParameterizedTest
    @CsvSource({"0, 1", "1, 2", "2, 4", "3, 8"})
    void schedulesExponentialBackoffAfterEachRetryableFailure(
            int failedAttempts, long delaySeconds) {
        var event = event();
        var failure = new IllegalStateException("smtp unavailable");
        when(deliveries.claim(any(), any(), any(), any(), any()))
                .thenReturn(new OrderConfirmationClaim.Acquired(failedAttempts, CLAIM_TOKEN));
        when(renderer.render(event)).thenReturn(RENDERED);
        doThrow(failure).when(sender).send(RENDERED);

        assertThatThrownBy(() -> service.send(event)).isSameAs(failure);

        verify(deliveries)
                .markFailed(
                        eq(EVENT_ID),
                        eq(CLAIM_TOKEN),
                        contains("smtp unavailable"),
                        eq(NOW.plusSeconds(delaySeconds)),
                        eq(false));
    }

    @Test
    void quarantinesTheFifthFailureWithoutSchedulingAnotherRetry() {
        var event = event();
        var failure = new IllegalStateException("smtp unavailable");
        when(deliveries.claim(any(), any(), any(), any(), any()))
                .thenReturn(new OrderConfirmationClaim.Acquired(4, CLAIM_TOKEN));
        when(renderer.render(event)).thenReturn(RENDERED);
        doThrow(failure).when(sender).send(RENDERED);

        assertThatThrownBy(() -> service.send(event)).isSameAs(failure);

        verify(deliveries)
                .markFailed(
                        eq(EVENT_ID),
                        eq(CLAIM_TOKEN),
                        contains("smtp unavailable"),
                        eq(NOW),
                        eq(true));
    }

    @Test
    void preservesTheOriginalFailureWhenRecordingTheFailureAlsoFails() {
        var event = event();
        var deliveryFailure = new IllegalStateException("smtp unavailable");
        var statusFailure = new StaleOrderConfirmationClaimException(EVENT_ID);
        when(deliveries.claim(any(), any(), any(), any(), any()))
                .thenReturn(new OrderConfirmationClaim.Acquired(0, CLAIM_TOKEN));
        when(renderer.render(event)).thenReturn(RENDERED);
        doThrow(deliveryFailure).when(sender).send(RENDERED);
        doThrow(statusFailure)
                .when(deliveries)
                .markFailed(any(), any(), any(), any(), anyBoolean());

        Throwable thrown = catchThrowable(() -> service.send(event));

        assertThat(thrown).isSameAs(deliveryFailure);
        assertThat(thrown.getSuppressed()).containsExactly(statusFailure);
    }

    @Test
    void doesNotAttemptToSuppressTheOriginalFailureIntoItself() {
        var event = event();
        var failure = new IllegalStateException("smtp and database unavailable");
        when(deliveries.claim(any(), any(), any(), any(), any()))
                .thenReturn(new OrderConfirmationClaim.Acquired(0, CLAIM_TOKEN));
        when(renderer.render(event)).thenReturn(RENDERED);
        doThrow(failure).when(sender).send(RENDERED);
        doThrow(failure).when(deliveries).markFailed(any(), any(), any(), any(), anyBoolean());

        Throwable thrown = catchThrowable(() -> service.send(event));

        assertThat(thrown).isSameAs(failure);
        assertThat(thrown.getSuppressed()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(OrderConfirmationClaim.Terminal.class)
    void skipsTheWorkflowAndStatusMutationForTerminalDelivery(
            OrderConfirmationClaim.Terminal terminal) {
        var event = event();
        when(deliveries.claim(
                        eq(EVENT_ID),
                        eq(ORDER_NUMBER),
                        any(UUID.class),
                        eq(NOW),
                        eq(NOW.plusSeconds(300))))
                .thenReturn(terminal);

        service.send(event);

        verifyNoInteractions(renderer, sender);
        verify(deliveries, never()).markSent(any(), any(), any());
        verify(deliveries, never()).markFailed(any(), any(), any(), any(), anyBoolean());
        verifyNoMoreInteractions(deliveries);
    }

    @Test
    void pendingDeliveryPropagatesItsEligibilityWithoutSendingOrChangingState() {
        Instant eligibleAt = NOW.plusSeconds(8);
        when(deliveries.claim(any(), any(), any(), any(), any()))
                .thenReturn(new OrderConfirmationClaim.Pending(eligibleAt));

        assertThatThrownBy(() -> service.send(event()))
                .isInstanceOfSatisfying(
                        OrderConfirmationPendingException.class,
                        pending -> assertThat(pending.nextEligibleAt()).isEqualTo(eligibleAt));
        verifyNoInteractions(renderer, sender);
        verify(deliveries, never()).markSent(any(), any(), any());
        verify(deliveries, never()).markFailed(any(), any(), any(), any(), anyBoolean());
    }

    private static OrderPlacedIntegrationEvent event() {
        return new OrderPlacedIntegrationEvent(
                EVENT_ID,
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                ORDER_NUMBER,
                42L,
                "Ada",
                "ada@example.com",
                NOW,
                new BigDecimal("39.98"),
                Currency.EUR,
                List.of(),
                new OrderPlacedIntegrationEvent.Address(
                        "Ada", null, "1 Main", null, "Berlin", null, "10115", "DE", null),
                new OrderPlacedIntegrationEvent.Address(
                        "Ada", null, "1 Main", null, "Berlin", null, "10115", "DE", null));
    }
}
