package com.xdev.ooms.security.user.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Soft-deleted users must not block username/email/phone reuse.
 * Drops global unique constraints/indexes on those columns and replaces them
 * with partial unique indexes that only cover active rows.
 */
@Component
@Order(1)
public class UserUniqueConstraintMigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UserUniqueConstraintMigrationRunner.class);
    private static final String[] USER_TABLES = {"oosmuser", "osmuser", "osm_user"};

    private final JdbcTemplate jdbcTemplate;

    public UserUniqueConstraintMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        for (String table : USER_TABLES) {
            if (!tableExists(table)) {
                continue;
            }
            dropLoginUniqueConstraints(table);
            dropLoginUniqueIndexes(table);
            releaseDeletedLoginIdentifiers(table);
        }

        if (!tableExists("oosmuser")) {
            return;
        }

        jdbcTemplate.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS uk_oosmuser_active_username
                    ON public.oosmuser (username)
                    WHERE COALESCE(is_deleted, FALSE) = FALSE
                """);
        jdbcTemplate.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS uk_oosmuser_active_email
                    ON public.oosmuser (LOWER(email))
                    WHERE COALESCE(is_deleted, FALSE) = FALSE
                      AND email IS NOT NULL
                """);
        jdbcTemplate.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS uk_oosmuser_active_phone
                    ON public.oosmuser (phone_number)
                    WHERE COALESCE(is_deleted, FALSE) = FALSE
                      AND phone_number IS NOT NULL
                """);

        log.info("Ensured active-only unique indexes on oosmuser username/email/phone");
    }

    private void dropLoginUniqueConstraints(String table) {
        jdbcTemplate.query(
                """
                        SELECT c.conname
                        FROM pg_constraint c
                        JOIN pg_class t ON t.oid = c.conrelid
                        JOIN pg_namespace n ON n.oid = t.relnamespace
                        WHERE n.nspname = 'public'
                          AND t.relname = ?
                          AND c.contype = 'u'
                          AND EXISTS (
                                SELECT 1
                                FROM unnest(c.conkey) AS colnum
                                JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = colnum
                                WHERE a.attname IN ('username', 'email', 'phone_number')
                          )
                        """,
                (rs, rowNum) -> rs.getString(1),
                table
        ).forEach(constraintName -> {
            jdbcTemplate.execute("ALTER TABLE public." + table + " DROP CONSTRAINT IF EXISTS " + constraintName);
            log.info("Dropped unique constraint {}.{}", table, constraintName);
        });
    }

    private void dropLoginUniqueIndexes(String table) {
        jdbcTemplate.query(
                """
                        SELECT i.relname
                        FROM pg_index x
                        JOIN pg_class t ON t.oid = x.indrelid
                        JOIN pg_class i ON i.oid = x.indexrelid
                        JOIN pg_namespace n ON n.oid = t.relnamespace
                        WHERE n.nspname = 'public'
                          AND t.relname = ?
                          AND x.indisunique
                          AND NOT x.indisprimary
                          AND i.relname NOT LIKE 'uk_oosmuser_active_%'
                          AND EXISTS (
                                SELECT 1
                                FROM unnest(x.indkey) AS colnum
                                JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = colnum
                                WHERE a.attname IN ('username', 'email', 'phone_number')
                          )
                        """,
                (rs, rowNum) -> rs.getString(1),
                table
        ).forEach(indexName -> {
            jdbcTemplate.execute("DROP INDEX IF EXISTS public." + indexName);
            log.info("Dropped unique index {}", indexName);
        });
    }

    private void releaseDeletedLoginIdentifiers(String table) {
        int updated = jdbcTemplate.update("""
                UPDATE public.%s
                SET username = CASE
                        WHEN username LIKE '%%#deleted#%%' THEN username
                        ELSE username || '#deleted#' || id::text
                    END,
                    email = CASE
                        WHEN email IS NULL OR email LIKE '%%#deleted#%%' THEN email
                        ELSE email || '#deleted#' || id::text
                    END,
                    phone_number = CASE
                        WHEN phone_number IS NULL OR phone_number LIKE '%%#deleted#%%' THEN phone_number
                        ELSE phone_number || '#deleted#' || id::text
                    END
                WHERE COALESCE(is_deleted, FALSE) = TRUE
                """.formatted(table));
        if (updated > 0) {
            log.info("Released login identifiers on {} soft-deleted row(s) in {}", updated, table);
        }
    }

    private boolean tableExists(String table) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM information_schema.tables
                        WHERE table_schema = 'public' AND table_name = ?
                        """,
                Integer.class,
                table
        );
        return count != null && count > 0;
    }
}
