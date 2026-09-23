package com.springbootecommerce.shophappens.ordering.notification.adapter.in.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.ordering.notification.application.port.in.OrderConfirmationPendingException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.listener.MessageListenerContainer;

class OrderConfirmationErrorHandlerTest {
    private static final Instant NOW = Instant.parse("2026-09-18T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    void delayRoundsUpSoSubmillisecondEligibilityDoesNotRetryEarly() {
        assertThat(OrderConfirmationErrorHandler.pendingDelay(NOW.plusNanos(1_234_001), CLOCK))
                .isEqualTo(2);
    }

    @Test
    void delayNeverBecomesNegativeWhenEligibilityPassesDuringHandling() {
        assertThat(OrderConfirmationErrorHandler.pendingDelay(NOW, CLOCK)).isZero();
        assertThat(OrderConfirmationErrorHandler.pendingDelay(NOW.minusSeconds(1), CLOCK)).isZero();
    }

    @Test
    void wrappedPendingFailuresKeepRetryingBeyondTheDefaultRecoveryLimit() {
        var handler = new OrderConfirmationErrorHandler(CLOCK);
        Consumer<?, ?> consumer = mock(Consumer.class);
        var container = mock(MessageListenerContainer.class);
        when(container.getContainerProperties()).thenReturn(new ContainerProperties("orders"));
        var record = new ConsumerRecord<>("orders", 0, 0, "key", "value");
        var failure =
                new ListenerExecutionFailedException(
                        "pending", new OrderConfirmationPendingException(NOW.minusSeconds(1)));

        for (int attempt = 0; attempt < 20; attempt++) {
            assertThatThrownBy(
                            () ->
                                    handler.handleRemaining(
                                            failure, List.of(record), consumer, container))
                    .isInstanceOf(RuntimeException.class)
                    .hasCause(failure);
        }
    }

    @Test
    void ordinaryFailuresKeepSpringKafkasDefaultRecoveryLimit() {
        var handler = new OrderConfirmationErrorHandler(CLOCK);
        Consumer<?, ?> consumer = mock(Consumer.class);
        var container = mock(MessageListenerContainer.class);
        when(container.getContainerProperties()).thenReturn(new ContainerProperties("orders"));
        var record = new ConsumerRecord<>("orders", 0, 0, "key", "value");
        var failure =
                new ListenerExecutionFailedException(
                        "failure", new IllegalStateException("unavailable"));

        for (int attempt = 0; attempt < 9; attempt++) {
            assertThatThrownBy(
                            () ->
                                    handler.handleRemaining(
                                            failure, List.of(record), consumer, container))
                    .isInstanceOf(RuntimeException.class)
                    .hasCause(failure);
        }
        assertThatCode(() -> handler.handleRemaining(failure, List.of(record), consumer, container))
                .doesNotThrowAnyException();
    }
}
