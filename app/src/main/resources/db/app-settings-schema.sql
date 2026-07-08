CREATE TABLE IF NOT EXISTS public.app_setting (
    id UUID PRIMARY KEY,
    setting_key VARCHAR(150) NOT NULL UNIQUE,
    value TEXT NULL,
    encrypted_value TEXT NULL,
    encryption_key_id VARCHAR(20) NOT NULL DEFAULT 'v1',
    value_type VARCHAR(50) NOT NULL,
    category VARCHAR(80) NOT NULL,
    label VARCHAR(150) NOT NULL,
    description TEXT NULL,
    sensitive BOOLEAN NOT NULL DEFAULT FALSE,
    editable BOOLEAN NOT NULL DEFAULT TRUE,
    restart_required BOOLEAN NOT NULL DEFAULT FALSE,
    required_for_feature BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by UUID NULL
);

CREATE INDEX IF NOT EXISTS idx_app_setting_category ON public.app_setting (category);

CREATE TABLE IF NOT EXISTS public.app_setting_audit (
    id UUID PRIMARY KEY,
    setting_key VARCHAR(150) NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_value_masked TEXT NULL,
    new_value_masked TEXT NULL,
    changed_by UUID NULL,
    changed_by_username VARCHAR(150) NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ip_address VARCHAR(100) NULL,
    user_agent TEXT NULL,
    reason TEXT NULL,
    success BOOLEAN NOT NULL,
    failure_reason TEXT NULL
);

CREATE INDEX IF NOT EXISTS idx_app_setting_audit_key ON public.app_setting_audit (setting_key);
CREATE INDEX IF NOT EXISTS idx_app_setting_audit_changed_at ON public.app_setting_audit (changed_at DESC);

CREATE TABLE IF NOT EXISTS public.app_setting_meta (
    id VARCHAR(50) PRIMARY KEY DEFAULT 'global',
    settings_version BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO public.app_setting_meta (id, settings_version, updated_at)
VALUES ('global', 0, NOW())
ON CONFLICT (id) DO NOTHING;
