package com.xdev.ooms.sharedkernel.mail.services.impl;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.xdev.ooms.sharedkernel.mail.config.OosmMailProperties;
import com.xdev.ooms.sharedkernel.mail.exception.MailDeliveryException;
import com.xdev.ooms.sharedkernel.mail.models.MailRequest;
import com.xdev.ooms.sharedkernel.mail.services.MailService;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MailServiceImpl implements MailService {

    private final ObjectProvider<Resend> resendClient;
    private final OosmMailProperties mailProperties;

    public MailServiceImpl(ObjectProvider<Resend> resendClient, OosmMailProperties mailProperties) {
        this.resendClient = resendClient;
        this.mailProperties = mailProperties;
    }

    @Override
    public boolean isDeliveryEnabled() {
        return mailProperties.isDeliveryEnabled();
    }

    @Override
    public void sendEmail(MailRequest request) throws MailDeliveryException {
        if (!isDeliveryEnabled()) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO,
                    "Mail delivery disabled; skipped email to %s", request.getTo());
            return;
        }

        Resend client = resendClient.getIfAvailable();
        if (client == null) {
            throw new MailDeliveryException("Resend client is not configured");
        }

        try {
            CreateEmailOptions.Builder builder = CreateEmailOptions.builder()
                    .from(mailProperties.getFormattedFrom())
                    .to(request.getTo())
                    .subject(request.getSubject());

            if (request.hasHtmlBody()) {
                builder.html(request.getHtmlBody());
                if (StringUtils.hasText(request.getBody())) {
                    builder.text(request.getBody());
                }
            } else {
                builder.text(request.getBody());
            }

            if (StringUtils.hasText(mailProperties.getSupportEmail())) {
                builder.replyTo(mailProperties.getSupportEmail());
            }

            CreateEmailResponse response = client.emails().send(builder.build());
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO,
                    "Email sent via Resend to %s (id=%s)", request.getTo(), response.getId());
        } catch (ResendException ex) {
            throw new MailDeliveryException("Failed to send email via Resend to " + request.getTo(), ex);
        }
    }
}
