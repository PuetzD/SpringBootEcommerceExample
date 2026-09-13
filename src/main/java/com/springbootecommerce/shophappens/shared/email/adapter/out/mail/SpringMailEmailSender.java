package com.springbootecommerce.shophappens.shared.email.adapter.out.mail;

import com.springbootecommerce.shophappens.shared.email.EmailMessage;
import com.springbootecommerce.shophappens.shared.email.EmailSender;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringMailEmailSender implements EmailSender {
    private final JavaMailSender mailSender;

    @Override
    public void send(EmailMessage message) {
        try {
            var mime = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mime, true, "UTF-8");
            helper.setFrom(message.from());
            helper.setTo(message.to().value());
            helper.setSubject(message.subject());
            helper.setText(message.textBody(), message.htmlBody());
            mailSender.send(mime);
        } catch (MessagingException exception) {
            throw new IllegalStateException("Could not build email message", exception);
        }
    }
}
