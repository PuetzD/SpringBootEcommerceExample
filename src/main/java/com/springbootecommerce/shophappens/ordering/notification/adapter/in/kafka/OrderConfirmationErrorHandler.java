package com.springbootecommerce.shophappens.ordering.notification.adapter.in.kafka;

import com.springbootecommerce.shophappens.ordering.notification.application.port.in.OrderConfirmationPendingException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.kafka.listener.DefaultBackOffHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.listener.SeekUtils;
import org.springframework.util.backoff.FixedBackOff;

class OrderConfirmationErrorHandler extends DefaultErrorHandler {
    OrderConfirmationErrorHandler(Clock clock) {
        super(
                null,
                SeekUtils.DEFAULT_BACK_OFF,
                new DefaultBackOffHandler() {
                    @Override
                    public void onNextBackOff(
                            MessageListenerContainer container,
                            Exception exception,
                            long defaultDelay) {
                        var pending = pendingCause(exception);
                        // Recompute for every failure: another worker may have renewed the lease.
                        long delay =
                                pending == null
                                        ? defaultDelay
                                        : pendingDelay(pending.nextEligibleAt(), clock);
                        super.onNextBackOff(container, exception, delay);
                    }
                });
        setBackOffFunction(
                (record, exception) ->
                        pendingCause(exception) == null
                                ? null
                                : new FixedBackOff(0, FixedBackOff.UNLIMITED_ATTEMPTS));
    }

    static long pendingDelay(Instant eligibleAt, Clock clock) {
        Duration remaining = Duration.between(clock.instant(), eligibleAt);
        if (remaining.isNegative() || remaining.isZero()) {
            return 0;
        }
        long millis = remaining.toMillis();
        return remaining.equals(Duration.ofMillis(millis)) ? millis : millis + 1;
    }

    private static OrderConfirmationPendingException pendingCause(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof OrderConfirmationPendingException pending) {
                return pending;
            }
        }
        return null;
    }
}
