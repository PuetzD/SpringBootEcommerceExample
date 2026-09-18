package com.springbootecommerce.shophappens.ordering.notification.adapter.out.email;

import static org.mockito.Mockito.verify;

import com.springbootecommerce.shophappens.ordering.notification.application.port.out.RenderedOrderConfirmation;
import com.springbootecommerce.shophappens.shared.email.EmailAddress;
import com.springbootecommerce.shophappens.shared.email.EmailMessage;
import com.springbootecommerce.shophappens.shared.email.EmailSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MailOrderConfirmationSenderTest {
    @Mock EmailSender emailSender;

    @Test
    void convertsTheRenderedConfirmationToTheSharedMailMessage() {
        var rendered =
                new RenderedOrderConfirmation(
                        "shop@example.com",
                        "ada@example.com",
                        "Order placed",
                        "text",
                        "<p>html</p>");

        new MailOrderConfirmationSender(emailSender).send(rendered);

        verify(emailSender)
                .send(
                        new EmailMessage(
                                "shop@example.com",
                                new EmailAddress("ada@example.com"),
                                "Order placed",
                                "text",
                                "<p>html</p>"));
    }
}
