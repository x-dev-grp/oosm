package com.xdev.ooms.security.user.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Copies tenant users left in legacy {@code osmuser} into {@code oosmuser} on startup.
 * Safe to re-run: skips usernames that already exist in {@code oosmuser}.
 */
@Component
@Order(0)
@ConditionalOnProperty(name = "app.security.legacy-user-migration.enabled", havingValue = "true", matchIfMissing = true)
public class LegacyOsmUserMigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LegacyOsmUserMigrationRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public LegacyOsmUserMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        if (!tableExists("osmuser")) {
            log.debug("Legacy osmuser table not found — skipping user migration");
            return;
        }

        log.info("Legacy osmuser table detected — migrating users to oosmuser");

        normalizeLegacyAdminUsername();

        if (!tableExists("oosmuser")) {
            jdbcTemplate.execute("ALTER TABLE osmuser RENAME TO oosmuser");
            log.info("Renamed osmuser to oosmuser");
            return;
        }

        int copied = copyMissingUsers();
        int repointed = repointForeignKeysToOosmUser();
        boolean dropped = tryDropLegacyTable();

        log.info(
                "Legacy user migration finished: {} user(s) copied, {} foreign key(s) repointed, legacy table dropped={}",
                copied,
                repointed,
                dropped
        );
    }

    private void normalizeLegacyAdminUsername() {
        jdbcTemplate.update("""
                UPDATE osmuser
                SET username = 'oosmAdmin', email = 'oosmAdmin@example.com'
                WHERE LOWER(username) = 'osmadmin'
                """);
    }

    private int copyMissingUsers() {
        return jdbcTemplate.update("""
                INSERT INTO oosmuser (
                    id, created_by, created_date, is_deleted, last_modified_by, last_modified_date,
                    qr_hex, qr_image_base64, tenant_id, confirmation_method, email, enabled, first_name,
                    is_locked, is_new_user, last_name, fcm_token, password, phone_number,
                    photo_content_type, photo_data, username, role_id
                )
                SELECT
                    o.id, o.created_by, o.created_date, o.is_deleted, o.last_modified_by, o.last_modified_date,
                    o.qr_hex, o.qr_image_base64, o.tenant_id, o.confirmation_method, o.email, o.enabled, o.first_name,
                    o.is_locked, o.is_new_user, o.last_name, o.one_signal_player_id, o.password, o.phone_number,
                    o.photo_content_type, o.photo_data, o.username, o.role_id
                FROM osmuser o
                WHERE NOT EXISTS (
                  SELECT 1 FROM oosmuser n WHERE LOWER(n.username) = LOWER(o.username)
                )
                """);
    }

    private int repointForeignKeysToOosmUser() {
        var foreignKeys = jdbcTemplate.query("""
                SELECT c.conname,
                       cl.relname AS child_table,
                       (
                         SELECT a.attname
                         FROM pg_attribute a
                         WHERE a.attrelid = c.conrelid
                           AND a.attnum = ANY (c.conkey)
                         ORDER BY a.attnum
                         LIMIT 1
                       ) AS child_column
                FROM pg_constraint c
                JOIN pg_class cl ON cl.oid = c.conrelid
                JOIN pg_namespace n ON n.oid = cl.relnamespace
                WHERE c.contype = 'f'
                  AND n.nspname = 'public'
                  AND c.confrelid = 'public.osmuser'::regclass
                """, (rs, rowNum) -> new ForeignKey(
                rs.getString("conname"),
                rs.getString("child_table"),
                rs.getString("child_column")
        ));

        for (ForeignKey fk : foreignKeys) {
            jdbcTemplate.execute(
                    "ALTER TABLE %s DROP CONSTRAINT %s".formatted(fk.childTable(), fk.constraintName())
            );
            jdbcTemplate.execute(
                    "ALTER TABLE %s ADD CONSTRAINT %s FOREIGN KEY (%s) REFERENCES oosmuser(id)"
                            .formatted(fk.childTable(), fk.constraintName(), fk.childColumn())
            );
            log.info("Repointed {}.{} from osmuser to oosmuser", fk.childTable(), fk.childColumn());
        }

        return foreignKeys.size();
    }

    private record ForeignKey(String constraintName, String childTable, String childColumn) {}

    private boolean tryDropLegacyTable() {
        if (!tableExists("osmuser")) {
            return false;
        }

        Integer unmigrated = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)::int
                FROM osmuser o
                WHERE NOT EXISTS (
                  SELECT 1 FROM oosmuser n WHERE LOWER(n.username) = LOWER(o.username)
                )
                """, Integer.class);

        if (unmigrated != null && unmigrated > 0) {
            log.warn("{} user(s) remain only in osmuser — legacy table kept", unmigrated);
            return false;
        }

        try {
            jdbcTemplate.execute("DROP TABLE osmuser");
            log.info("Dropped legacy osmuser table");
            return true;
        } catch (Exception ex) {
            log.warn("Could not drop legacy osmuser table: {}", ex.getMessage());
            return false;
        }
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)::int
                FROM information_schema.tables
                WHERE table_schema = 'public' AND table_name = ?
                """, Integer.class, tableName);
        return count != null && count > 0;
    }
}
