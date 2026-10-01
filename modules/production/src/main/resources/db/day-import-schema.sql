CREATE TABLE IF NOT EXISTS day_import_run (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    file_digest VARCHAR(64) NOT NULL,
    source VARCHAR(32) NOT NULL,
    actor VARCHAR(255) NOT NULL,
    outcome VARCHAR(32) NOT NULL,
    report TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS ix_day_import_run_tenant ON day_import_run(tenant_id, created_at);
CREATE UNIQUE INDEX IF NOT EXISTS uk_day_import_committed_file
    ON day_import_run(tenant_id, file_digest) WHERE outcome = 'COMMITTED';

CREATE TABLE IF NOT EXISTS day_import_operation (
    tenant_id UUID NOT NULL,
    operation_kind VARCHAR(32) NOT NULL,
    external_ref VARCHAR(255) NOT NULL,
    payload_digest VARCHAR(64) NOT NULL,
    entity_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, operation_kind, external_ref)
);

CREATE TABLE IF NOT EXISTS day_import_drive_state (
    tenant_id UUID PRIMARY KEY,
    lease_id UUID,
    lease_until TIMESTAMPTZ,
    status TEXT NOT NULL DEFAULT '{}'
);
CREATE TABLE IF NOT EXISTS day_import_drive_file (
    tenant_id UUID NOT NULL,
    file_id VARCHAR(255) NOT NULL,
    file_digest VARCHAR(64) NOT NULL,
    target_folder VARCHAR(255) NOT NULL,
    routed BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY(tenant_id, file_id)
);

-- Existing databases need a non-null version on previously created balances.
ALTER TABLE IF EXISTS storage_unit ADD COLUMN IF NOT EXISTS balance_version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE IF EXISTS oil_container ADD COLUMN IF NOT EXISTS balance_version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE IF EXISTS unified_delivery ADD COLUMN IF NOT EXISTS balance_version BIGINT NOT NULL DEFAULT 0;
