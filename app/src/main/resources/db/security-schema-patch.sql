-- Align confirmation_code tables with ConfirmationCode entity (failedAttempts tracking).

ALTER TABLE IF EXISTS public.confirmation_code
    ADD COLUMN IF NOT EXISTS failed_attempts integer NOT NULL DEFAULT 0;

ALTER TABLE IF EXISTS public.confirmation_code_aud
    ADD COLUMN IF NOT EXISTS failed_attempts integer;

-- JWT access tokens can exceed 10k when many permissions are embedded in claims.
ALTER TABLE IF EXISTS public.authorization
    ALTER COLUMN access_token_value TYPE TEXT,
    ALTER COLUMN refresh_token_value TYPE TEXT,
    ALTER COLUMN authorization_code_value TYPE TEXT,
    ALTER COLUMN access_token_scopes TYPE TEXT,
    ALTER COLUMN oidc_id_token_value TYPE TEXT,
    ALTER COLUMN user_code_value TYPE TEXT,
    ALTER COLUMN device_code_value TYPE TEXT;

ALTER TABLE IF EXISTS public.osm_user
    ADD COLUMN IF NOT EXISTS photo_data TEXT,
    ADD COLUMN IF NOT EXISTS photo_content_type VARCHAR(50);

-- Company logo is stored as base64 (~200KB); ensure column accepts large payloads.
ALTER TABLE IF EXISTS public.company_profile
    ALTER COLUMN logo_data TYPE TEXT;

-- If refresh/login fails with OSMUser serialVersionUID after entity changes, clear stored OAuth2
-- authorizations once (users must sign in again). Uncomment only when needed:
-- TRUNCATE TABLE public.authorization;
