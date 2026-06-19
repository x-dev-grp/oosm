package com.xdev.ooms.sharedkernel.mail.templates;

import com.xdev.ooms.sharedkernel.mail.config.OsmMailProperties;
import com.xdev.ooms.sharedkernel.mail.models.EmailBranding;
import com.xdev.ooms.sharedkernel.mail.models.MailRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;

@Component
public class OsmMailComposer {

    private static final int RESET_CODE_EXPIRY_MINUTES = 10;

    private final EmailTemplateRenderer templateRenderer;
    private final OsmMailProperties mailProperties;

    public OsmMailComposer(EmailTemplateRenderer templateRenderer, OsmMailProperties mailProperties) {
        this.templateRenderer = templateRenderer;
        this.mailProperties = mailProperties;
    }

    public EmailBranding defaultBranding() {
        return EmailBranding.defaults(
                mailProperties.getFromName(),
                mailProperties.getFrontendBaseUrl(),
                mailProperties.getSupportEmail()
        );
    }

    public MailRequest composeWelcomeCredentials(
            String recipientEmail,
            String username,
            String temporaryPassword,
            EmailBranding branding
    ) throws IOException {
        EmailBranding resolved = branding != null ? branding : defaultBranding();
        Map<String, String> variables = baseVariables(resolved);
        variables.put("USERNAME", username);
        variables.put("TEMP_PASSWORD", temporaryPassword);
        variables.put("LOGIN_URL", resolved.loginUrl());
        variables.put("PREHEADER", "Vos identifiants de connexion OSM");

        String htmlBody = templateRenderer.render("mail/welcome-credentials.html", variables);
        String plainBody = """
                Bonjour,

                Votre compte %s a ete cree.

                Identifiant : %s
                Mot de passe temporaire : %s

                Connectez-vous : %s

                Changez votre mot de passe apres la premiere connexion.
                Si vous n'avez pas demande ce compte, ignorez cet e-mail.

                %s
                """.formatted(
                resolved.getCompanyName(),
                username,
                temporaryPassword,
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

    public MailRequest composePasswordReset(
            String recipientEmail,
            String resetCode,
            String userId,
            EmailBranding branding
    ) throws IOException {
        EmailBranding resolved = branding != null ? branding : defaultBranding();
        Map<String, String> variables = baseVariables(resolved);
        variables.put("RESET_CODE", resetCode);
        variables.put("RESET_URL", resolved.resetUrl(userId));
        variables.put("EXPIRY_MINUTES", String.valueOf(RESET_CODE_EXPIRY_MINUTES));
        variables.put("PREHEADER", "Code de reinitialisation de mot de passe");

        String htmlBody = templateRenderer.render("mail/password-reset.html", variables);
        String plainBody = """
                Bonjour,

                Vous avez demande la reinitialisation de votre mot de passe %s.

                Code de confirmation : %s
                Ce code expire dans %d minutes.

                Saisissez le code ici : %s

                Si vous n'etes pas a l'origine de cette demande, ignorez cet e-mail.

                %s
                """.formatted(
                resolved.getProductName(),
                resetCode,
                RESET_CODE_EXPIRY_MINUTES,
                resolved.resetUrl(userId),
                resolved.getSupportEmail()
        );

        MailRequest request = new MailRequest();
        request.setTo(recipientEmail);
        request.setSubject("Reinitialisation de votre mot de passe " + resolved.getProductName());
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
}
