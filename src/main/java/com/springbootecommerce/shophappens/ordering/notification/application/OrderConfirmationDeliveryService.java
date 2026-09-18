package com.springbootecommerce.shophappens.ordering.notification.application;

import com.springbootecommerce.shophappens.ordering.application.event.OrderPlacedIntegrationEvent;
import com.springbootecommerce.shophappens.ordering.notification.application.port.in.SendOrderConfirmationUseCase;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationDelivery;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationRenderer;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationSender;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderConfirmationDeliveryService implements SendOrderConfirmationUseCase {
    private static final Duration CLAIM_LEASE = Duration.ofMinutes(5);

    private final OrderConfirmationDelivery deliveries;
    private final OrderConfirmationRenderer renderer;
    private final OrderConfirmationSender sender;
    private final Clock clock;

    @Override
    public void send(OrderPlacedIntegrationEvent event) {
        Instant now = clock.instant();
        var claim =
                deliveries.claim(event.eventId(), event.orderNumber(), now, now.plus(CLAIM_LEASE));
        if (claim.isEmpty()) {
            return;
        }

        try {
            sender.send(renderer.render(event));
            deliveries.markSent(event.eventId(), clock.instant());
        } catch (RuntimeException exception) {
            int attempt = claim.orElseThrow().failedAttempts() + 1;
            boolean quarantine = attempt >= 5;
            Instant retryAt =
                    quarantine ? clock.instant() : clock.instant().plusSeconds(1L << (attempt - 1));
            try {
                deliveries.markFailed(event.eventId(), diagnostic(exception), retryAt, quarantine);
            } catch (RuntimeException markFailedException) {
                if (markFailedException != exception) {
                    exception.addSuppressed(markFailedException);
                }
            }
            throw exception;
        }
    }

    private static String diagnostic(Throwable exception) {
        Throwable cause = exception.getCause() == null ? exception : exception.getCause();
        String message = cause.getMessage();
        return cause.getClass().getSimpleName() + (message == null ? "" : ": " + message);
    }
}
