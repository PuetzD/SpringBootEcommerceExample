package com.springbootecommerce.shophappens.ordering.notification.adapter.out.email;

import com.springbootecommerce.shophappens.ordering.notification.application.port.out.OrderConfirmationSender;
import com.springbootecommerce.shophappens.ordering.notification.application.port.out.RenderedOrderConfirmation;
import com.springbootecommerce.shophappens.shared.email.EmailAddress;
import com.springbootecommerce.shophappens.shared.email.EmailMessage;
import com.springbootecommerce.shophappens.shared.email.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailOrderConfirmationSender implements OrderConfirmationSender {
    private final EmailSender sender;

    @Override
    public void send(RenderedOrderConfirmation confirmation) {
        sender.send(
                new EmailMessage(
                        confirmation.from(),
                        new EmailAddress(confirmation.to()),
                        confirmation.subject(),
                        confirmation.textBody(),
                        confirmation.htmlBody()));
    }
}
