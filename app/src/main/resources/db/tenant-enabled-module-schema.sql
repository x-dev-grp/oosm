CREATE TABLE IF NOT EXISTS public.tenant_enabled_module (
    id UUID PRIMARY KEY,
    company_tenant_id UUID NOT NULL,
    module VARCHAR(32) NOT NULL,
    tenant_id UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_by VARCHAR(255),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(255),
    last_modified_date TIMESTAMP,
    CONSTRAINT uq_tenant_enabled_module UNIQUE (company_tenant_id, module)
);

CREATE INDEX IF NOT EXISTS idx_tenant_enabled_module_company
    ON public.tenant_enabled_module (company_tenant_id);
