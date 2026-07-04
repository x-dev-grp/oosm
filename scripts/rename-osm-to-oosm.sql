-- OSM → OOSM migration (run once on existing databases before deploying the renamed application)
-- Safe to re-run: uses IF EXISTS / conditional updates where possible.

-- OAuth client
UPDATE oauth2_registered_client
SET client_id = 'oosm-client'
WHERE client_id = 'osm-client';

-- Platform admin role
UPDATE role
SET role_name = 'OOSMADMIN'
WHERE role_name = 'OSMADMIN';

-- Permission entity keys (HABILITATION:OSMUSER:* → HABILITATION:OOSMUSER:*)
UPDATE permission
SET entity = 'OOSMUSER'
WHERE entity = 'OSMUSER';

-- Bootstrap admin username (optional — only if you use the default account)
UPDATE osmuser
SET username = 'oosmAdmin', email = 'oosmAdmin@example.com'
WHERE LOWER(username) = 'osmadmin';

UPDATE oosmuser
SET username = 'oosmAdmin',
    email    = 'oosmAdmin@example.com'
WHERE LOWER(username) = 'osmadmin';

-- Rename user table when only legacy name exists
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = 'public' AND table_name = 'osmuser'
  ) AND NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = 'public' AND table_name = 'oosmuser'
  ) THEN
    ALTER TABLE osmuser RENAME TO oosmuser;
  END IF;
END $$;

-- When both tables exist (Hibernate created empty oosmuser), copy missing users by username
INSERT INTO oosmuser (id, created_by, created_date, is_deleted, last_modified_by, last_modified_date,
                      qr_hex, qr_image_base64, tenant_id, confirmation_method, email, enabled, first_name,
                      is_locked, is_new_user, last_name, one_signal_player_id, password, phone_number,
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
WHERE EXISTS (
  SELECT 1 FROM information_schema.tables
  WHERE table_schema = 'public' AND table_name = 'osmuser'
)
AND NOT EXISTS (
  SELECT 1 FROM oosmuser n WHERE LOWER(n.username) = LOWER(o.username)
);
