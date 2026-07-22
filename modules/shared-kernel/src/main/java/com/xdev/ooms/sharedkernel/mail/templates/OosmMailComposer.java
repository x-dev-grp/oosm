package com.xdev.ooms.sharedkernel.mail.templates;

import com.xdev.ooms.sharedkernel.mail.config.DynamicMailSettings;
import com.xdev.ooms.sharedkernel.mail.models.EmailBranding;
import com.xdev.ooms.sharedkernel.mail.models.MailRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class OosmMailComposer {

    private static final int RESET_CODE_EXPIRY_MINUTES = 10;
    private static final int TEMP_PASSWORD_EXPIRY_HOURS = 24;
    private static final DateTimeFormatter EXPIRY_AT_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm (z)", Locale.FRENCH);

    private final EmailTemplateRenderer templateRenderer;
    private final DynamicMailSettings mailSettings;

    public OosmMailComposer(EmailTemplateRenderer templateRenderer, DynamicMailSettings mailSettings) {
        this.templateRenderer = templateRenderer;
        this.mailSettings = mailSettings;
    }

    public EmailBranding defaultBranding() {
        return EmailBranding.defaults(
                mailSettings.getFromName(),
                mailSettings.getFrontendBaseUrl(),
                mailSettings.getSupportEmail()
        );
    }

    public MailRequest composeWelcomeCredentials(
            String recipientEmail,
            String username,
            String temporaryPassword,
            EmailBranding branding
    ) throws IOException {
        EmailBranding resolved = branding != null ? branding : defaultBranding();
        String expiryAt = formatExpiryAt(Duration.ofHours(TEMP_PASSWORD_EXPIRY_HOURS));
        Map<String, String> variables = baseVariables(resolved);
        variables.put("USERNAME", username);
        variables.put("TEMP_PASSWORD", temporaryPassword);
        variables.put("LOGIN_URL", resolved.loginUrl());
        variables.put("EXPIRY_HOURS", String.valueOf(TEMP_PASSWORD_EXPIRY_HOURS));
        variables.put("EXPIRY_AT", expiryAt);
        variables.put("PREHEADER", "Identifiants valables jusqu'au " + expiryAt);

        String htmlBody = templateRenderer.render("mail/welcome-credentials.html", variables);
        String plainBody = """
                Bonjour,

                Votre compte %s a été créé.

                Identifiant : %s
                Mot de passe temporaire : %s

                Ce mot de passe temporaire est valable %d heures, jusqu'au %s.
                Changez-le dès votre première connexion.

                Connectez-vous : %s

                Si vous n'avez pas demandé ce compte, ignorez cet e-mail.

                %s
                """.formatted(
                resolved.getCompanyName(),
                username,
                temporaryPassword,
                TEMP_PASSWORD_EXPIRY_HOURS,
                expiryAt,
                resolved.loginUrl(),
                resolved.getSupportEmail()
        );

        MailRequest request = new MailRequest();
        request.setTo(recipientEmail);
        request.setSubject("Bienvenue sur " + resolved.getProductName() + " — vos identifiants");
        request.setBody(plainBody);
        request.setHtmlBody(htmlBody);
        return request;
    }

    public MailRequest composeAdminPasswordReset(
            String recipientEmail,
            String username,
            String temporaryPassword,
            EmailBranding branding
    ) throws IOException {
        EmailBranding resolved = branding != null ? branding : defaultBranding();
        String expiryAt = formatExpiryAt(Duration.ofHours(TEMP_PASSWORD_EXPIRY_HOURS));
        Map<String, String> variables = baseVariables(resolved);
        variables.put("USERNAME", username);
        variables.put("TEMP_PASSWORD", temporaryPassword);
        variables.put("LOGIN_URL", resolved.loginUrl());
        variables.put("EXPIRY_HOURS", String.valueOf(TEMP_PASSWORD_EXPIRY_HOURS));
        variables.put("EXPIRY_AT", expiryAt);
        variables.put("PREHEADER", "Mot de passe temporaire valable jusqu'au " + expiryAt);

        String htmlBody = templateRenderer.render("mail/admin-password-reset.html", variables);
        String plainBody = """
                Bonjour,

                Un administrateur a réinitialisé le mot de passe de votre compte %s.

                Identifiant : %s
                Mot de passe temporaire : %s

                Ce mot de passe temporaire est valable %d heures, jusqu'au %s.
                Changez-le dès votre première connexion.

                Connectez-vous : %s

                Si vous n'êtes pas à l'origine de cette demande, contactez immédiatement le support.

                %s
                """.formatted(
                resolved.getProductName(),
                username,
                temporaryPassword,
                TEMP_PASSWORD_EXPIRY_HOURS,
                expiryAt,
                resolved.loginUrl(),
                resolved.getSupportEmail()
        );

        MailRequest request = new MailRequest();
        request.setTo(recipientEmail);
        request.setSubject("Réinitialisation de votre mot de passe " + resolved.getProductName());
        request.setBody(plainBody);
        request.setHtmlBody(htmlBody);
        return request;
    }

    public int getTempPasswordExpiryHours() {
        return TEMP_PASSWORD_EXPIRY_HOURS;
    }

    public MailRequest composePasswordReset(
            String recipientEmail,
            String resetCode,
            String userId,
            EmailBranding branding
    ) throws IOException {
        EmailBranding resolved = branding != null ? branding : defaultBranding();
        String expiryAt = formatExpiryAt(Duration.ofMinutes(RESET_CODE_EXPIRY_MINUTES));
        Map<String, String> variables = baseVariables(resolved);
        variables.put("RESET_CODE", resetCode);
        variables.put("RESET_URL", resolved.resetUrl(userId));
        variables.put("EXPIRY_MINUTES", String.valueOf(RESET_CODE_EXPIRY_MINUTES));
        variables.put("EXPIRY_AT", expiryAt);
        variables.put("PREHEADER", "Code valable jusqu'au " + expiryAt);

        String htmlBody = templateRenderer.render("mail/password-reset.html", variables);
        String plainBody = """
                Bonjour,

                Vous avez demandé la réinitialisation de votre mot de passe %s.

                Code de confirmation : %s
                Ce code expire dans %d minutes, soit le %s.

                Saisissez le code ici : %s

                Si vous n'êtes pas à l'origine de cette demande, ignorez cet e-mail.

                %s
                """.formatted(
                resolved.getProductName(),
                resetCode,
                RESET_CODE_EXPIRY_MINUTES,
                expiryAt,
                resolved.resetUrl(userId),
                resolved.getSupportEmail()
        );

        MailRequest request = new MailRequest();
        request.setTo(recipientEmail);
        request.setSubject("Réinitialisation de votre mot de passe " + resolved.getProductName());
        request.setBody(plainBody);
        request.setHtmlBody(htmlBody);
        return request;
    }

    private Map<String, String> baseVariables(EmailBranding branding) {
        Map<String, String> variables = new HashMap<>();
        variables.put("COMPANY_NAME", branding.getCompanyName());
        variables.put("PRODUCT_NAME", branding.getProductName());
        variables.put("SUPPORT_EMAIL", branding.getSupportEmail());
        variables.put("YEAR", String.valueOf(Year.now().getValue()));
        return variables;
    }

    private String formatExpiryAt(Duration lifetime) {
        ZonedDateTime expiry = ZonedDateTime.now(ZoneId.systemDefault()).plus(lifetime);
        return EXPIRY_AT_FORMATTER.format(expiry);
    }
}
