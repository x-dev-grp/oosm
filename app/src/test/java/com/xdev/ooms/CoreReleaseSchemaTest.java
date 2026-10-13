package com.xdev.ooms;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreReleaseSchemaTest {

    @Test
    void coreSchemaOwnsBalanceVersionCompatibilityOutsideDayImport() throws Exception {
        String coreSchema = read("db/core-release-schema.sql");
        String dayImportSchema = read("db/day-import-schema.sql");

        for (String table : new String[]{"storage_unit", "oil_container", "unified_delivery"}) {
            assertTrue(coreSchema.contains("public." + table));
        }
        assertTrue(coreSchema.contains("ADD COLUMN IF NOT EXISTS balance_version BIGINT DEFAULT 0"));
        assertTrue(coreSchema.contains("SET balance_version = 0 WHERE balance_version IS NULL"));
        assertTrue(coreSchema.contains("ALTER COLUMN balance_version SET DEFAULT 0"));
        assertTrue(coreSchema.contains("ALTER COLUMN balance_version SET NOT NULL"));
        assertFalse(dayImportSchema.contains("balance_version"));
    }

    @Test
    void applicationAlwaysLoadsCoreSchemaButNotDeferredDayImportSchema() throws Exception {
        String configuration = read("application.yml");

        assertTrue(configuration.contains("defer-datasource-initialization: true"));
        assertTrue(configuration.contains("classpath:db/core-release-schema.sql"));
        assertFalse(configuration.contains("classpath:db/day-import-schema.sql"));
    }

    private String read(String path) throws Exception {
        return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
    }
}
