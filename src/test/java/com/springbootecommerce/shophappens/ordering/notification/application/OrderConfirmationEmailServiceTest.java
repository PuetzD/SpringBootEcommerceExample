package com.springbootecommerce.shophappens.ordering.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.springbootecommerce.shophappens.ordering.application.event.OrderPlacedIntegrationEvent;
import com.springbootecommerce.shophappens.shared.email.EmailMessage;
import com.springbootecommerce.shophappens.shared.email.EmailSender;
import com.springbootecommerce.shophappens.shared.email.EmailTemplateRenderer;
import com.springbootecommerce.shophappens.sharedkernel.money.Currency;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@ExtendWith(MockitoExtension.class)
class OrderConfirmationEmailServiceTest {
    @Mock EmailSender sender;

    @Test
    void rendersThePlacementSnapshotWithoutPaymentOrShippingClaims() {
        var service =
                new OrderConfirmationEmailService(templateEngine(), sender, "shop@example.com");

        service.send(event());

        ArgumentCaptor<EmailMessage> message = ArgumentCaptor.forClass(EmailMessage.class);
        verify(sender).send(message.capture());
        assertThat(message.getValue().to().value()).isEqualTo("ada@example.com");
        assertThat(message.getValue().subject()).isEqualTo("Order ORD-2026-100001 placed");
        assertThat(message.getValue().textBody())
                .contains("Hello Ada", "ORD-2026-100001", "SHIRT-L", "EUR 39.98", "1 Main Street")
                .doesNotContain("paid", "shipped", "payment confirmed");
        assertThat(message.getValue().htmlBody()).contains("Headphones", "EUR 39.98");
    }

    private static OrderPlacedIntegrationEvent event() {
        return new OrderPlacedIntegrationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ORD-2026-100001",
                42L,
                "Ada",
                "ada@example.com",
                Instant.parse("2026-09-13T10:00:00Z"),
                new BigDecimal("39.98"),
                Currency.EUR,
                List.of(
                        new OrderPlacedIntegrationEvent.Item(
                                1, 2, "SHIRT-L", "Headphones", new BigDecimal("19.99"), 2)),
                new OrderPlacedIntegrationEvent.Address(
                        "Ada", null, "1 Main Street", null, "Berlin", null, "10115", "DE", null),
                new OrderPlacedIntegrationEvent.Address(
                        "Ada", null, "1 Main Street", null, "Berlin", null, "10115", "DE", null));
    }

    private static EmailTemplateRenderer templateEngine() {
        var html = new ClassLoaderTemplateResolver();
        html.setPrefix("templates/");
        html.setSuffix(".html");
        html.setTemplateMode(TemplateMode.HTML);
        html.setCharacterEncoding("UTF-8");
        html.setOrder(1);
        html.setResolvablePatterns(java.util.Set.of("email/order-confirmation-html"));

        var text = new ClassLoaderTemplateResolver();
        text.setPrefix("templates/");
        text.setSuffix(".txt");
        text.setTemplateMode(TemplateMode.TEXT);
        text.setCharacterEncoding("UTF-8");
        text.setOrder(2);
        text.setResolvablePatterns(java.util.Set.of("email/order-confirmation-text"));

        var engine = new SpringTemplateEngine();
        engine.setTemplateResolvers(java.util.Set.of(html, text));
        return (template, locale, variables) ->
                engine.process(template, new Context(locale, variables));
    }
}
