-- =============================================================================
-- Drop Hibernate Envers audit tables (keep live data tables)
-- =============================================================================
-- Removes:
--   - every public table whose name ends with "_aud"
--   - revinfo / revinfo_seq (Envers revision metadata)
--
-- Keeps all normal data tables (no "_aud" suffix).
--
-- Run in psql:
--   psql -h localhost -U postgres -d osm -f drop-envers-audit-tables.sql
--
-- Or run the whole file in pgAdmin / DBeaver (execute script, not single line).
-- =============================================================================

-- Optional preview (uncomment to inspect before dropping):
-- SELECT tablename
-- FROM pg_tables
-- WHERE schemaname = 'public'
--   AND tablename ~ '_aud$'
-- ORDER BY tablename;

DO $drop_envers_aud$
DECLARE
    audit_table_name text;
BEGIN
    FOR audit_table_name IN
        SELECT t.tablename
        FROM pg_catalog.pg_tables t
        WHERE t.schemaname = 'public'
          AND t.tablename ~ '_aud$'
        ORDER BY t.tablename
    LOOP
        EXECUTE format('DROP TABLE IF EXISTS %I.%I CASCADE', 'public', audit_table_name);
        RAISE NOTICE 'Dropped table: public.%', audit_table_name;
    END LOOP;
END
$drop_envers_aud$;

DROP TABLE IF EXISTS public.revinfo CASCADE;
DROP SEQUENCE IF EXISTS public.revinfo_seq;
DROP TABLE IF EXISTS public.revision_info CASCADE;
DROP SEQUENCE IF EXISTS public.revision_info_seq;

-- Verification: must return 0 rows
SELECT tablename
FROM pg_catalog.pg_tables
WHERE schemaname = 'public'
  AND (
        tablename ~ '_aud$'
     OR tablename IN ('revinfo', 'revision_info')
  )
ORDER BY tablename;

-- =============================================================================
-- ALTERNATIVE (if your SQL client does not support DO blocks)
-- Step 1: run this query and execute every row it returns:
--
--   SELECT 'DROP TABLE IF EXISTS public.' || quote_ident(tablename) || ' CASCADE;'
--   FROM pg_catalog.pg_tables
--   WHERE schemaname = 'public'
--     AND tablename ~ '_aud$'
--   ORDER BY tablename;
--
-- Step 2: then run:
--   DROP TABLE IF EXISTS public.revinfo CASCADE;
--   DROP SEQUENCE IF EXISTS public.revinfo_seq;
-- =============================================================================
