-- Idempotent compatibility patch required by the non-Excel release.
-- These additive columns are safe to leave in place when rolling back the application.

ALTER TABLE public.storage_unit
    ADD COLUMN IF NOT EXISTS balance_version BIGINT DEFAULT 0;
UPDATE public.storage_unit SET balance_version = 0 WHERE balance_version IS NULL;
ALTER TABLE public.storage_unit
    ALTER COLUMN balance_version SET DEFAULT 0,
    ALTER COLUMN balance_version SET NOT NULL;

ALTER TABLE public.oil_container
    ADD COLUMN IF NOT EXISTS balance_version BIGINT DEFAULT 0;
UPDATE public.oil_container SET balance_version = 0 WHERE balance_version IS NULL;
ALTER TABLE public.oil_container
    ALTER COLUMN balance_version SET DEFAULT 0,
    ALTER COLUMN balance_version SET NOT NULL;

ALTER TABLE public.unified_delivery
    ADD COLUMN IF NOT EXISTS balance_version BIGINT DEFAULT 0;
UPDATE public.unified_delivery SET balance_version = 0 WHERE balance_version IS NULL;
ALTER TABLE public.unified_delivery
    ALTER COLUMN balance_version SET DEFAULT 0,
    ALTER COLUMN balance_version SET NOT NULL;
