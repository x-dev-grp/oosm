-- Align confirmation_code tables with ConfirmationCode entity (failedAttempts tracking).

ALTER TABLE IF EXISTS public.confirmation_code
    ADD COLUMN IF NOT EXISTS failed_attempts integer NOT NULL DEFAULT 0;

ALTER TABLE IF EXISTS public.confirmation_code_aud
    ADD COLUMN IF NOT EXISTS failed_attempts integer;
