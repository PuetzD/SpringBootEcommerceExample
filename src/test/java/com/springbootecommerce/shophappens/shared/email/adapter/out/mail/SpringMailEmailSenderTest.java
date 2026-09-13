package com.springbootecommerce.shophappens.shared.email.adapter.out.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.springbootecommerce.shophappens.shared.email.EmailAddress;
import com.springbootecommerce.shophappens.shared.email.EmailMessage;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class SpringMailEmailSenderTest {
    @Mock JavaMailSender mailSender;

    @Test
    void mapsTheEmailMessageToAPlainAndHtmlMimeMessage() throws Exception {
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        org.mockito.Mockito.when(mailSender.createMimeMessage()).thenReturn(message);
        var sender = new SpringMailEmailSender(mailSender);

        sender.send(
                new EmailMessage(
                        "shop@example.com",
                        new EmailAddress("ada@example.com"),
                        "Order placed",
                        "Plain confirmation",
                        "<p>HTML confirmation</p>"));

        ArgumentCaptor<MimeMessage> sent = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(sent.capture());
        assertThat(sent.getValue().getFrom()[0].toString()).isEqualTo("shop@example.com");
        assertThat(
                        sent.getValue()
                                .getRecipients(jakarta.mail.Message.RecipientType.TO)[0]
                                .toString())
                .isEqualTo("ada@example.com");
        assertThat(sent.getValue().getSubject()).isEqualTo("Order placed");
        var raw = new ByteArrayOutputStream();
        sent.getValue().writeTo(raw);
        assertThat(raw.toString()).contains("Plain confirmation");
        assertThat(raw.toString()).contains("HTML confirmation");
    }
}
