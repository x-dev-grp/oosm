package com.xdev.ooms.production.dayimport.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Applies {@code db/day-import-schema.sql} on every start, because deployments run with
 * {@code SPRING_SQL_INIT_MODE=never} and the day-import ledger tables are not managed by Hibernate.
 * Every statement in the script must stay idempotent.
 */
@Component
public class DayImportSchemaInitializer {

    static final String SCRIPT = "db/day-import-schema.sql";
    private static final Logger log = LoggerFactory.getLogger(DayImportSchemaInitializer.class);

    private final DataSource dataSource;
    private final boolean enabled;

    public DayImportSchemaInitializer(DataSource dataSource,
                                      @Value("${oosm.day-import.schema-init.enabled:true}") boolean enabled) {
        this.dataSource = dataSource;
        this.enabled = enabled;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureSchema() {
        if (!enabled) {
            return;
        }
        try {
            new ResourceDatabasePopulator(new ClassPathResource(SCRIPT)).execute(dataSource);
            log.info("Day import schema verified ({})", SCRIPT);
        } catch (RuntimeException e) {
            log.error("Day import schema could not be applied; day imports will fail until {} is run", SCRIPT, e);
        }
    }
}
