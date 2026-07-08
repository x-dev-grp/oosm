package com.xdev.ooms.sharedkernel.mail.services.impl;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.xdev.ooms.sharedkernel.mail.config.DynamicMailSettings;
import com.xdev.ooms.sharedkernel.mail.config.MailProvider;
import com.xdev.ooms.sharedkernel.mail.exception.MailDeliveryException;
import com.xdev.ooms.sharedkernel.mail.models.MailRequest;
import com.xdev.ooms.sharedkernel.mail.services.MailService;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MailServiceImpl implements MailService {

    private final DynamicMailSettings mailSettings;
    private final SmtpMailSender smtpMailSender;

    public MailServiceImpl(DynamicMailSettings mailSettings, SmtpMailSender smtpMailSender) {
        this.mailSettings = mailSettings;
        this.smtpMailSender = smtpMailSender;
    }

    @Override
    public boolean isDeliveryEnabled() {
        return mailSettings.isDeliveryEnabled();
    }

    @Override
    public void sendEmail(MailRequest request) throws MailDeliveryException {
        if (!isDeliveryEnabled()) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO,
                    "Mail delivery disabled; skipped email to %s", request.getTo());
            return;
        }

        if (mailSettings.getProvider() == MailProvider.SMTP) {
            smtpMailSender.send(request);
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO,
                    "Email sent via SMTP to %s", request.getTo());
            return;
        }

        sendViaResend(request);
    }

    private void sendViaResend(MailRequest request) throws MailDeliveryException {
        String apiKey = mailSettings.getApiKey().orElse(null);
        if (!StringUtils.hasText(apiKey)) {
            throw new MailDeliveryException("Resend client is not configured");
        }

        Resend client = new Resend(apiKey);

        try {
            CreateEmailOptions.Builder builder = CreateEmailOptions.builder()
                    .from(mailSettings.getFormattedFrom())
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

            if (StringUtils.hasText(mailSettings.getSupportEmail())) {
                builder.replyTo(mailSettings.getSupportEmail());
            }

            CreateEmailResponse response = client.emails().send(builder.build());
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO,
                    "Email sent via Resend to %s (id=%s)", request.getTo(), response.getId());
        } catch (ResendException ex) {
            throw new MailDeliveryException(formatResendError(ex), ex);
        }
    }

    private static String formatResendError(ResendException ex) {
        String raw = ex.getMessage();
        if (raw == null) {
            return "Resend rejected the email";
        }
        if (raw.contains("domain is not verified") || raw.contains("validation_error")) {
            return "Resend rejected the sender: the From address domain is not verified. "
                    + "Add and verify the domain at https://resend.com/domains, or use an address on a verified domain.";
        }
        return "Resend rejected the email: " + raw;
    }
}
