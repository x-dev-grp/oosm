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
WHERE username = 'osmAdmin';

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
INSERT INTO oosmuser
SELECT o.*
FROM osmuser o
WHERE EXISTS (
  SELECT 1 FROM information_schema.tables
  WHERE table_schema = 'public' AND table_name = 'osmuser'
)
AND NOT EXISTS (
  SELECT 1 FROM oosmuser n WHERE LOWER(n.username) = LOWER(o.username)
);
