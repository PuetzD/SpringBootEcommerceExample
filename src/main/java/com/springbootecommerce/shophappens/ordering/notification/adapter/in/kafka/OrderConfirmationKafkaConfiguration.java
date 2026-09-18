package com.springbootecommerce.shophappens.ordering.notification.adapter.in.kafka;

import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.kafka.autoconfigure.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        name = "notifications.email.enabled",
        havingValue = "true",
        matchIfMissing = true)
class OrderConfirmationKafkaConfiguration {
    @Bean
    ConcurrentKafkaListenerContainerFactory<Object, Object>
            orderConfirmationKafkaListenerContainerFactory(
                    ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
                    ConsumerFactory<Object, Object> consumerFactory,
                    Clock clock) {
        var factory = new ConcurrentKafkaListenerContainerFactory<Object, Object>();
        configurer.configure(factory, consumerFactory);
        factory.setCommonErrorHandler(new OrderConfirmationErrorHandler(clock));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }
}
