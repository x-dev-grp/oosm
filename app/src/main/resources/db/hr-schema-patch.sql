-- =============================================================================
-- Drop legacy external_id columns (BaseEntity no longer maps this field)
-- =============================================================================
-- Safe to re-run: uses oosm_schema_patch tracking and IF EXISTS guards.
--
-- Does NOT touch financial_transaction.external_transaction_id (finance link).
--
-- Run manually in psql:
--   psql -h localhost -U postgres -d osm -f drop-external-id-columns.sql
--
-- Or execute the whole file in pgAdmin / DBeaver.
-- =============================================================================

CREATE TABLE IF NOT EXISTS public.oosm_schema_patch (
                                                        patch_id VARCHAR(128) PRIMARY KEY,
                                                        applied_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Optional preview (uncomment to inspect before dropping):
-- SELECT table_name
-- FROM information_schema.columns
-- WHERE table_schema = 'public'
--   AND column_name = 'external_id'
-- ORDER BY table_name;
--
-- SELECT schemaname, tablename, indexname, indexdef
-- FROM pg_indexes
-- WHERE schemaname = 'public'
--   AND indexdef ILIKE '%external_id%'
-- ORDER BY tablename, indexname;

DO $drop_external_id$
    DECLARE
        patch_id constant text := 'drop-external-id-columns-v1';
        idx record;
        col record;
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM public.oosm_schema_patch
            WHERE oosm_schema_patch.patch_id = patch_id
        ) THEN
            RAISE NOTICE 'Patch % already applied — skipping', patch_id;
            RETURN;
        END IF;

        FOR idx IN
            SELECT i.schemaname, i.indexname
            FROM pg_catalog.pg_indexes i
            WHERE i.schemaname = 'public'
              AND i.indexdef ILIKE '%external_id%'
            ORDER BY i.tablename, i.indexname
            LOOP
                EXECUTE format('DROP INDEX IF EXISTS %I.%I', idx.schemaname, idx.indexname);
                RAISE NOTICE 'Dropped index: %.%', idx.schemaname, idx.indexname;
            END LOOP;

        FOR col IN
            SELECT c.table_schema, c.table_name
            FROM information_schema.columns c
            WHERE c.table_schema = 'public'
              AND c.column_name = 'external_id'
            ORDER BY c.table_name
            LOOP
                EXECUTE format(
                        'ALTER TABLE %I.%I DROP COLUMN IF EXISTS external_id',
                        col.table_schema,
                        col.table_name
                        );
                RAISE NOTICE 'Dropped column external_id from %.%', col.table_schema, col.table_name;
            END LOOP;

        INSERT INTO public.oosm_schema_patch (patch_id)
        VALUES (patch_id);
    END
$drop_external_id$;

-- Verification: must return 0 rows
SELECT table_name
FROM information_schema.columns
WHERE table_schema = 'public'
  AND column_name = 'external_id'
ORDER BY table_name;
