package com.springbootecommerce.shophappens.ordering.notification.application.port.out;

public record RenderedOrderConfirmation(
        String from, String to, String subject, String textBody, String htmlBody) {
    public RenderedOrderConfirmation {
        from = required(from, "Sender");
        to = required(to, "Recipient");
        subject = required(subject, "Subject");
        textBody = required(textBody, "Text body");
        htmlBody = required(htmlBody, "HTML body");
    }

    private static String required(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value;
    }
}
