package com.xdev.ooms.production.parameter.config;

import com.xdev.ooms.sharedkernel.utils.OSMLogger;

import com.xdev.ooms.production.parameter.repository.ParameterRepo;
import com.xdev.ooms.production.parameter.service.ParameterService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.UUID;

@Configuration
@ConditionalOnProperty(name = "app.production.bootstrap.enabled", havingValue = "true", matchIfMissing = true)
public class ProductionParameterBootstrap {

    @Bean
    CommandLineRunner ensureProductionParameters(ParameterService parameterService, ParameterRepo parameterRepo) {
        return args -> {
            List<UUID> tenantIds = parameterRepo.findDistinctTenantIds();
            if (tenantIds.isEmpty()) {
                OSMLogger.debug(ProductionParameterBootstrap.class, "No tenants found for production parameter bootstrap");
                return;
            }

            OSMLogger.info(ProductionParameterBootstrap.class, "Ensuring default application parameters for {} tenant(s)", tenantIds.size());
            for (UUID tenantId : tenantIds) {
                parameterService.ensureDefaultsForTenant(tenantId);
            }
        };
    }
}
