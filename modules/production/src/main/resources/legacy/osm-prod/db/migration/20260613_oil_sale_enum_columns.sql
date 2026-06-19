-- Fix oil_sale.payment_method when migrating from ordinal (integer) to string storage.
-- Run manually if you later switch OilSale.paymentMethod to @Enumerated(EnumType.STRING).
-- Hibernate ddl-auto alone fails because legacy check constraints compare integers.

DO $$
DECLARE
    constraint_name text;
BEGIN
    FOR constraint_name IN
        SELECT c.conname
        FROM pg_constraint c
                 JOIN pg_class t ON c.conrelid = t.oid
                 JOIN pg_namespace n ON t.relnamespace = n.oid
                 JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = ANY (c.conkey)
        WHERE n.nspname = 'public'
          AND t.relname = 'oil_sale'
          AND a.attname = 'payment_method'
          AND c.contype = 'c'
    LOOP
        EXECUTE format('ALTER TABLE public.oil_sale DROP CONSTRAINT IF EXISTS %I', constraint_name);
    END LOOP;
END $$;

ALTER TABLE public.oil_sale
    ADD COLUMN IF NOT EXISTS payment_method_tmp VARCHAR(255);

UPDATE public.oil_sale
SET payment_method_tmp = CASE payment_method
    WHEN 0 THEN 'CASH'
    WHEN 1 THEN 'CHEQUE'
    WHEN 2 THEN 'TRANSFER'
    WHEN 3 THEN 'OIL'
    WHEN 4 THEN 'MIXED'
    ELSE NULL
END
WHERE payment_method_tmp IS NULL
  AND payment_method IS NOT NULL;

ALTER TABLE public.oil_sale DROP COLUMN IF EXISTS payment_method;
ALTER TABLE public.oil_sale RENAME COLUMN payment_method_tmp TO payment_method;

-- Optional: same pattern for currency if it was stored as ordinal
DO $$
DECLARE
    constraint_name text;
BEGIN
    FOR constraint_name IN
        SELECT c.conname
        FROM pg_constraint c
                 JOIN pg_class t ON c.conrelid = t.oid
                 JOIN pg_namespace n ON t.relnamespace = n.oid
                 JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = ANY (c.conkey)
        WHERE n.nspname = 'public'
          AND t.relname = 'oil_sale'
          AND a.attname = 'currency'
          AND c.contype = 'c'
    LOOP
        EXECUTE format('ALTER TABLE public.oil_sale DROP CONSTRAINT IF EXISTS %I', constraint_name);
    END LOOP;
END $$;

ALTER TABLE public.oil_sale
    ADD COLUMN IF NOT EXISTS currency_tmp VARCHAR(255);

UPDATE public.oil_sale
SET currency_tmp = CASE currency
    WHEN 0 THEN 'TND'
    WHEN 1 THEN 'EUR'
    WHEN 2 THEN 'USD'
    ELSE NULL
END
WHERE currency_tmp IS NULL
  AND currency IS NOT NULL;

ALTER TABLE public.oil_sale DROP COLUMN IF EXISTS currency;
ALTER TABLE public.oil_sale RENAME COLUMN currency_tmp TO currency;
