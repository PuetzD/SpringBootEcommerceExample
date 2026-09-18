package com.springbootecommerce.shophappens.ordering.notification.adapter.out.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.springbootecommerce.shophappens.ordering.application.event.OrderPlacedIntegrationEvent;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.RenderedOrderConfirmation;
import com.springbootecommerce.shophappens.shared.email.EmailTemplateRenderer;
import com.springbootecommerce.shophappens.sharedkernel.money.Currency;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

class TemplateOrderConfirmationRendererTest {

    @Test
    void rendersThePlacementSnapshotWithoutPaymentOrShippingClaims() {
        var renderer = new TemplateOrderConfirmationRenderer(templateEngine(), "shop@example.com");

        RenderedOrderConfirmation confirmation = renderer.render(event());

        assertThat(confirmation.from()).isEqualTo("shop@example.com");
        assertThat(confirmation.to()).isEqualTo("ada@example.com");
        assertThat(confirmation.subject()).isEqualTo("Order ORD-2026-100001 placed");
        assertThat(confirmation.textBody())
                .contains("Hello Ada", "ORD-2026-100001", "SHIRT-L", "EUR 39.98", "1 Main Street")
                .doesNotContain("paid", "shipped", "payment confirmed");
        assertThat(confirmation.htmlBody()).contains("Headphones", "EUR 39.98");
    }

    @ParameterizedTest(name = "rejects blank {0}")
    @MethodSource("blankRequiredValues")
    void rejectsBlankRequiredValues(String field, ThrowingCallable construction) {
        assertThatThrownBy(construction).isInstanceOf(IllegalArgumentException.class);
    }

    private static Stream<Arguments> blankRequiredValues() {
        return Stream.of(
                arguments(
                        "from",
                        (ThrowingCallable)
                                () ->
                                        new RenderedOrderConfirmation(
                                                " ", "ada@example.com", "subject", "text", "html")),
                arguments(
                        "to",
                        (ThrowingCallable)
                                () ->
                                        new RenderedOrderConfirmation(
                                                "shop@example.com",
                                                " ",
                                                "subject",
                                                "text",
                                                "html")),
                arguments(
                        "subject",
                        (ThrowingCallable)
                                () ->
                                        new RenderedOrderConfirmation(
                                                "shop@example.com",
                                                "ada@example.com",
                                                " ",
                                                "text",
                                                "html")),
                arguments(
                        "text body",
                        (ThrowingCallable)
                                () ->
                                        new RenderedOrderConfirmation(
                                                "shop@example.com",
                                                "ada@example.com",
                                                "subject",
                                                " ",
                                                "html")),
                arguments(
                        "HTML body",
                        (ThrowingCallable)
                                () ->
                                        new RenderedOrderConfirmation(
                                                "shop@example.com",
                                                "ada@example.com",
                                                "subject",
                                                "text",
                                                " ")));
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
