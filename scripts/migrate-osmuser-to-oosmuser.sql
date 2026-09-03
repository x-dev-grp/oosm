-- Copy tenant users left behind in legacy osmuser into oosmuser (skip duplicates by username).
-- Safe to re-run. Also repoints FKs (e.g. confirmation_code.user_id) and drops osmuser when empty.

UPDATE osmuser
SET username = 'oosmAdmin',
    email    = 'oosmAdmin@example.com'
WHERE LOWER(username) = 'osmadmin';

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'osmuser') AND NOT EXISTS (SELECT 1
                                                                 FROM information_schema.tables
                                                                 WHERE table_schema = 'public'
                                                                   AND table_name = 'oosmuser') THEN
            ALTER TABLE osmuser
                RENAME TO oosmuser;
            RETURN;
        END IF;
    END
$$;

INSERT INTO oosmuser (id, created_by, created_date, is_deleted, last_modified_by, last_modified_date,
                      qr_hex, qr_image_base64, tenant_id, confirmation_method, email, enabled, first_name,
                      is_locked, is_new_user, last_name, fcm_token, password, phone_number,
                      photo_content_type, photo_data, username, role_id)
SELECT o.id,
       o.created_by,
       o.created_date,
       o.is_deleted,
       o.last_modified_by,
       o.last_modified_date,
       o.qr_hex,
       o.qr_image_base64,
       o.tenant_id,
       o.confirmation_method,
       o.email,
       o.enabled,
       o.first_name,
       o.is_locked,
       o.is_new_user,
       o.last_name,
       o.one_signal_player_id,
       o.password,
       o.phone_number,
       o.photo_content_type,
       o.photo_data,
       o.username,
       o.role_id
FROM osmuser o
WHERE EXISTS (SELECT 1
              FROM information_schema.tables
              WHERE table_schema = 'public'
                AND table_name = 'osmuser')
  AND NOT EXISTS (
  SELECT 1 FROM oosmuser n WHERE LOWER(n.username) = LOWER(o.username)
);

DO $$
    DECLARE
        r RECORD;
BEGIN
        IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = 'public' AND table_name = 'osmuser'
  ) THEN
            RETURN;
        END IF;

        FOR r IN
            SELECT c.conname,
                   cl.relname AS child_table,
                   (SELECT a.attname
                    FROM pg_attribute a
                    WHERE a.attrelid = c.conrelid
                      AND a.attnum = ANY (c.conkey)
                    ORDER BY a.attnum
                    LIMIT 1)  AS child_column
            FROM pg_constraint c
                     JOIN pg_class cl ON cl.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = cl.relnamespace
            WHERE c.contype = 'f'
              AND n.nspname = 'public'
              AND c.confrelid = 'public.osmuser'::regclass
            LOOP
                EXECUTE format('ALTER TABLE %I DROP CONSTRAINT %I', r.child_table, r.conname);
                EXECUTE format(
                        'ALTER TABLE %I ADD CONSTRAINT %I FOREIGN KEY (%I) REFERENCES oosmuser(id)',
                        r.child_table, r.conname, r.child_column
                        );
            END LOOP;

        IF NOT EXISTS (SELECT 1
                       FROM osmuser o
                       WHERE NOT EXISTS (SELECT 1
                                         FROM oosmuser n
                                         WHERE LOWER(n.username) = LOWER(o.username))) THEN
            DROP TABLE osmuser;
  END IF;
END $$;
