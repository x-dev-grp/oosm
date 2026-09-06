-- Per-tenant Google Drive OAuth credentials (encrypted refresh token).
CREATE TABLE IF NOT EXISTS tenant_google_drive_credential (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_by VARCHAR(255),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(255),
    last_modified_date TIMESTAMP,
    google_account_email VARCHAR(320),
    encrypted_refresh_token VARCHAR(4000) NOT NULL,
    scope VARCHAR(1000),
    connected_at TIMESTAMP,
    token_updated_at TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_tenant_gdrive_active
    ON tenant_google_drive_credential (tenant_id)
    WHERE is_deleted = FALSE;
