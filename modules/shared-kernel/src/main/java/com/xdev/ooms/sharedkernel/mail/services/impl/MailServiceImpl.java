package com.xdev.ooms.sharedkernel.mail.services.impl;

import com.xdev.ooms.sharedkernel.mail.config.OosmMailProperties;
import com.xdev.ooms.sharedkernel.mail.models.MailRequest;
import com.xdev.ooms.sharedkernel.mail.services.MailService;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;

@Service
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final OosmMailProperties mailProperties;

    public MailServiceImpl(JavaMailSender mailSender, OosmMailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }

    @Override
    public boolean isDeliveryEnabled() {
        return mailProperties.isDeliveryEnabled();
    }

    @Override
    public void sendEmail(MailRequest request) throws MessagingException {
        if (!isDeliveryEnabled()) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO,
                    "Mail delivery disabled; skipped email to {}", request.getTo());
            return;
        }

        MimeMessage message = mailSender.createMimeMessage();
        boolean multipart = request.hasHtmlBody();
        MimeMessageHelper helper = new MimeMessageHelper(message, multipart, "UTF-8");

        if (StringUtils.hasText(mailProperties.getFromAddress())) {
            helper.setFrom(buildFromAddress());
        }

        helper.setTo(request.getTo());
        helper.setSubject(request.getSubject());

        if (request.hasHtmlBody()) {
            helper.setText(request.getBody(), request.getHtmlBody());
        } else {
            helper.setText(request.getBody(), false);
        }

        mailSender.send(message);
    }

    private InternetAddress buildFromAddress() throws MessagingException {
        try {
            return new InternetAddress(
                    mailProperties.getFromAddress(),
                    mailProperties.getFromName(),
                    "UTF-8"
            );
        } catch (UnsupportedEncodingException ex) {
            try {
                return new InternetAddress(mailProperties.getFromAddress());
            } catch (Exception fallback) {
                throw new MessagingException("Unable to build from address", fallback);
            }
        }
    }
}
