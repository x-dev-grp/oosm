-- Optional manual migration when HIBERNATE_DDL_AUTO=none.
-- Adds company profile branding / locale columns used by general-config.

ALTER TABLE company_profile
    ADD COLUMN IF NOT EXISTS creation_date date,
    ADD COLUMN IF NOT EXISTS invoice_footer_note text,
    ADD COLUMN IF NOT EXISTS invoice_legal_mentions text,
    ADD COLUMN IF NOT EXISTS preferred_theme_color varchar(40),
    ADD COLUMN IF NOT EXISTS default_language varchar(10),
    ADD COLUMN IF NOT EXISTS timezone varchar(60),
    ADD COLUMN IF NOT EXISTS pwa_short_name varchar(40),
    ADD COLUMN IF NOT EXISTS invoice_bank_name varchar(120),
    ADD COLUMN IF NOT EXISTS invoice_bank_iban varchar(64),
    ADD COLUMN IF NOT EXISTS invoice_bank_swift varchar(32);
