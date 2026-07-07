package com.xdev.ooms.sharedkernel.mail.config;

import com.resend.Resend;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.mail.resend", name = "api-key")
    public Resend resendClient(OosmMailProperties mailProperties) {
        return new Resend(mailProperties.getApiKey());
    }
}
