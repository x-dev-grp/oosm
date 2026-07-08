package com.xdev.ooms.sharedkernel.mail.services.impl;

import com.xdev.ooms.sharedkernel.mail.config.DynamicMailSettings;
import com.xdev.ooms.sharedkernel.mail.exception.MailDeliveryException;
import com.xdev.ooms.sharedkernel.mail.models.MailRequest;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Properties;

@Component
public class SmtpMailSender {

    private static final int SMTP_TIMEOUT_MS = 15_000;

    private final DynamicMailSettings mailSettings;

    public SmtpMailSender(DynamicMailSettings mailSettings) {
        this.mailSettings = mailSettings;
    }

    public void send(MailRequest request) throws MailDeliveryException {
        String host = mailSettings.getSmtpHost();
        int port = mailSettings.getSmtpPort();
        boolean ssl = usesImplicitSsl(port);

        OOSMLogger.info(this.getClass(),
                "SMTP send starting: host={} port={} auth={} startTls={} ssl={} from={} to={}",
                host, port, mailSettings.isSmtpAuth(), mailSettings.isSmtpStartTls(), ssl,
                mailSettings.getFromAddress(), request.getTo());

        try {
            Session session = createSession(host, port, ssl);
            MimeMessage message = buildMessage(session, request);
            Transport.send(message);
            OOSMLogger.info(this.getClass(), "SMTP send completed: host={} port={} to={}", host, port, request.getTo());
        } catch (MessagingException ex) {
            OOSMLogger.warn(this.getClass(),
                    "SMTP send failed: host={} port={} to={} error={}",
                    host, port, request.getTo(), ex.getMessage());
            throw new MailDeliveryException(formatSmtpError(host, port, ex), ex);
        } catch (RuntimeException ex) {
            throw new MailDeliveryException("Failed to send email via SMTP to " + request.getTo(), ex);
        }
    }

    private MimeMessage buildMessage(Session session, MailRequest request) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(mailSettings.getFromAddress(), mailSettings.getFromName(), "UTF-8"));
        } catch (java.io.UnsupportedEncodingException ex) {
            message.setFrom(new InternetAddress(mailSettings.getFromAddress()));
        }
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(request.getTo(), false));
        message.setSubject(request.getSubject(), "UTF-8");

        if (StringUtils.hasText(mailSettings.getSupportEmail())) {
            message.setReplyTo(new InternetAddress[]{new InternetAddress(mailSettings.getSupportEmail())});
        }

        if (request.hasHtmlBody()) {
            MimeMultipart multipart = new MimeMultipart("alternative");
            if (StringUtils.hasText(request.getBody())) {
                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(request.getBody(), "UTF-8");
                multipart.addBodyPart(textPart);
            }
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(request.getHtmlBody(), "text/html; charset=UTF-8");
            multipart.addBodyPart(htmlPart);
            message.setContent(multipart);
        } else {
            message.setText(request.getBody(), "UTF-8");
        }
        return message;
    }

    private Session createSession(String host, int port, boolean ssl) {
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));
        props.put("mail.smtp.auth", String.valueOf(mailSettings.isSmtpAuth()));
        props.put("mail.smtp.connectiontimeout", String.valueOf(SMTP_TIMEOUT_MS));
        props.put("mail.smtp.timeout", String.valueOf(SMTP_TIMEOUT_MS));
        props.put("mail.smtp.writetimeout", String.valueOf(SMTP_TIMEOUT_MS));

        if (ssl) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.port", String.valueOf(port));
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
        } else if (mailSettings.isSmtpStartTls()) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }

        if (mailSettings.isDebug()) {
            props.put("mail.debug", "true");
        }

        if (!mailSettings.isSmtpAuth()) {
            return Session.getInstance(props);
        }

        String username = mailSettings.getSmtpUsername();
        String password = mailSettings.getSmtpPassword().orElse("");
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    private static boolean usesImplicitSsl(int port) {
        return port == 465;
    }

    static String formatSmtpError(String host, int port, MessagingException ex) {
        String message = rootMessage(ex);
        if (!StringUtils.hasText(message)) {
            return "SMTP server rejected the email";
        }

        if (isConnectionFailure(message) && "smtp.google.com".equalsIgnoreCase(host)) {
            return "SMTP connection failed to smtp.google.com. Gmail uses smtp.gmail.com (not smtp.google.com). "
                    + "Set SMTP host to smtp.gmail.com, port 587, STARTTLS on, and use a Google App Password.";
        }

        if (isConnectionFailure(message)) {
            return "SMTP connection failed to " + host + ":" + port + ". "
                    + "Check host/port, firewall, and whether the server requires STARTTLS (587) or SSL (465). "
                    + "Details: " + message;
        }

        if (message.contains("535") || message.toLowerCase().contains("authentication failed")) {
            return "SMTP authentication failed for " + host + ". "
                    + "Verify username/password. Gmail requires an App Password when 2FA is enabled.";
        }

        return "SMTP error: " + message;
    }

    private static boolean isConnectionFailure(String message) {
        String lower = message.toLowerCase();
        return lower.contains("couldn't connect")
                || lower.contains("connect timed out")
                || lower.contains("connection refused")
                || lower.contains("mailconnectexception")
                || lower.contains("unknown host");
    }

    private static String rootMessage(Throwable ex) {
        Throwable current = ex;
        String lastMessage = ex.getMessage();
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
            if (StringUtils.hasText(current.getMessage())) {
                lastMessage = current.getMessage();
            }
        }
        return lastMessage;
    }
}
