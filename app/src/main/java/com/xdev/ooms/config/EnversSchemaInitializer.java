package com.xdev.ooms.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ensures Envers revision metadata exists when schema is managed by Hibernate (Flyway disabled).
 */
@Component
public class EnversSchemaInitializer {

    private final JdbcTemplate jdbcTemplate;

    public EnversSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureRevinfoObjects() {
        jdbcTemplate.execute("""
                CREATE SEQUENCE IF NOT EXISTS revinfo_seq
                    START WITH 1
                    INCREMENT BY 50
                    NO MINVALUE
                    NO MAXVALUE
                    CACHE 1
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS revinfo (
                    rev INTEGER NOT NULL,
                    revtstmp BIGINT,
                    PRIMARY KEY (rev)
                )
                """);
    }
}
