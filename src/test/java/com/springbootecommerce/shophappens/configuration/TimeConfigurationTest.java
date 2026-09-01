package com.springbootecommerce.shophappens.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class TimeConfigurationTest {
    @Test
    void suppliesASystemUtcClockBean() {
        try (var context = new AnnotationConfigApplicationContext(TimeConfiguration.class)) {
            Clock clock = context.getBean(Clock.class);

            assertThat(clock.getZone()).isEqualTo(ZoneOffset.UTC);
        }
    }
}
