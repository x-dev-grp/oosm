package com.xdev.ooms.production.parameter.config;

import com.xdev.ooms.production.parameter.repository.ParameterRepo;
import com.xdev.ooms.production.parameter.service.ParameterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.UUID;

@Configuration
@ConditionalOnProperty(name = "app.production.bootstrap.enabled", havingValue = "true", matchIfMissing = true)
public class ProductionParameterBootstrap {

    private static final Logger log = LoggerFactory.getLogger(ProductionParameterBootstrap.class);

    @Bean
    CommandLineRunner ensureProductionParameters(ParameterService parameterService, ParameterRepo parameterRepo) {
        return args -> {
            List<UUID> tenantIds = parameterRepo.findDistinctTenantIds();
            if (tenantIds.isEmpty()) {
                log.debug("No tenants found for production parameter bootstrap");
                return;
            }

            log.info("Ensuring default application parameters for {} tenant(s)", tenantIds.size());
            for (UUID tenantId : tenantIds) {
                parameterService.ensureDefaultsForTenant(tenantId);
            }
        };
    }
}
