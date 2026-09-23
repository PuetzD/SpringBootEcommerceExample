package com.springbootecommerce.shophappens.ordering.notification.adapter.out.email;

import com.springbootecommerce.shophappens.ordering.application.event.OrderPlacedIntegrationEvent;
import com.springbootecommerce.shophappens.ordering.application.event.OrderPlacedIntegrationEvent.Address;
import com.springbootecommerce.shophappens.ordering.application.event.OrderPlacedIntegrationEvent.Item;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationRenderer;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.RenderedOrderConfirmation;
import com.springbootecommerce.shophappens.shared.email.EmailTemplateRenderer;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TemplateOrderConfirmationRenderer implements OrderConfirmationRenderer {
    private final EmailTemplateRenderer templates;
    private final String from;

    public TemplateOrderConfirmationRenderer(
            EmailTemplateRenderer templates,
            @Value("${notifications.email.from:shop@example.com}") String from) {
        this.templates = templates;
        this.from = from;
    }

    @Override
    public RenderedOrderConfirmation render(OrderPlacedIntegrationEvent event) {
        var variables = variables(event);
        String html = templates.render("email/order-confirmation-html", Locale.ROOT, variables);
        String text = templates.render("email/order-confirmation-text", Locale.ROOT, variables);
        return new RenderedOrderConfirmation(
                from,
                event.customerContactEmail(),
                "Order " + event.orderNumber() + " placed",
                text,
                html);
    }

    private static Map<String, Object> variables(OrderPlacedIntegrationEvent event) {
        var variables = new HashMap<String, Object>();
        variables.put("givenName", event.customerGivenName());
        variables.put("orderNumber", event.orderNumber());
        variables.put("placedAt", event.occurredAt().toString());
        variables.put("total", money(event.currency().name(), event.total()));
        variables.put(
                "items",
                event.items().stream().map(item -> item(item, event.currency().name())).toList());
        variables.put("shippingAddress", address(event.shippingAddress()));
        variables.put("billingAddress", address(event.billingAddress()));
        return variables;
    }

    private static Map<String, Object> item(Item item, String currency) {
        return Map.of(
                "productName", item.productName(),
                "sku", item.sku(),
                "quantity", item.quantity(),
                "unitPrice", money(currency, item.unitPrice()),
                "lineTotal",
                        money(
                                currency,
                                item.unitPrice().multiply(BigDecimal.valueOf(item.quantity()))));
    }

    private static List<String> address(Address address) {
        return java.util.stream.Stream.of(
                        address.recipientName(),
                        address.companyName(),
                        address.addressLine1(),
                        address.addressLine2(),
                        address.city(),
                        address.region(),
                        address.postalCode(),
                        address.countryCode())
                .filter(value -> value != null && !value.isBlank())
                .toList();
    }

    private static String money(String currency, BigDecimal amount) {
        return currency + " " + amount.setScale(2).toPlainString();
    }
}
