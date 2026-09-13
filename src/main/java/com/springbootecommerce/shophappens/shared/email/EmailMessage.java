package com.springbootecommerce.shophappens.shared.email;

import java.util.Objects;

public record EmailMessage(
        String from, EmailAddress to, String subject, String textBody, String htmlBody) {
    public EmailMessage {
        if (from == null || from.isBlank())
            throw new IllegalArgumentException("Sender is required");
        if (subject == null || subject.isBlank())
            throw new IllegalArgumentException("Subject is required");
        if (textBody == null || textBody.isBlank())
            throw new IllegalArgumentException("Text body is required");
        if (htmlBody == null || htmlBody.isBlank())
            throw new IllegalArgumentException("HTML body is required");
        Objects.requireNonNull(to, "Recipient is required");
    }
}
