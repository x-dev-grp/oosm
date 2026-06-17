-- Olive oil product compliance fields (Tunisia traceability & labeling).

ALTER TABLE IF EXISTS skus
    ADD COLUMN IF NOT EXISTS ingredient_declaration text,
    ADD COLUMN IF NOT EXISTS storage_conditions text,
    ADD COLUMN IF NOT EXISTS shelf_life_months integer DEFAULT 24,
    ADD COLUMN IF NOT EXISTS acidity_level varchar(20),
    ADD COLUMN IF NOT EXISTS peroxide_value varchar(40),
    ADD COLUMN IF NOT EXISTS k232 varchar(40),
    ADD COLUMN IF NOT EXISTS k270 varchar(40),
    ADD COLUMN IF NOT EXISTS polyphenol_content varchar(40),
    ADD COLUMN IF NOT EXISTS olive_varieties text,
    ADD COLUMN IF NOT EXISTS harvest_region varchar(120),
    ADD COLUMN IF NOT EXISTS organic boolean DEFAULT false,
    ADD COLUMN IF NOT EXISTS organic_cert_number varchar(120),
    ADD COLUMN IF NOT EXISTS organic_cert_body varchar(120),
    ADD COLUMN IF NOT EXISTS organic_cert_expiry date,
    ADD COLUMN IF NOT EXISTS supplier_name varchar(255),
    ADD COLUMN IF NOT EXISTS supplier_code varchar(120),
    ADD COLUMN IF NOT EXISTS supplier_contact varchar(255),
    ADD COLUMN IF NOT EXISTS olive_source_type varchar(40),
    ADD COLUMN IF NOT EXISTS olive_source_reference varchar(255),
    ADD COLUMN IF NOT EXISTS production_batch_ref varchar(120),
    ADD COLUMN IF NOT EXISTS extraction_batch_ref varchar(120),
    ADD COLUMN IF NOT EXISTS product_status varchar(40) DEFAULT 'DRAFT',
    ADD COLUMN IF NOT EXISTS nutrition_declaration_json text,
    ADD COLUMN IF NOT EXISTS brand_description text;

UPDATE skus
SET shelf_life_months = COALESCE(shelf_life_months, 24),
    organic = COALESCE(organic, false),
    product_status = COALESCE(product_status, 'DRAFT');

ALTER TABLE IF EXISTS skus_aud
    ADD COLUMN IF NOT EXISTS ingredient_declaration text,
    ADD COLUMN IF NOT EXISTS storage_conditions text,
    ADD COLUMN IF NOT EXISTS shelf_life_months integer,
    ADD COLUMN IF NOT EXISTS acidity_level varchar(20),
    ADD COLUMN IF NOT EXISTS peroxide_value varchar(40),
    ADD COLUMN IF NOT EXISTS k232 varchar(40),
    ADD COLUMN IF NOT EXISTS k270 varchar(40),
    ADD COLUMN IF NOT EXISTS polyphenol_content varchar(40),
    ADD COLUMN IF NOT EXISTS olive_varieties text,
    ADD COLUMN IF NOT EXISTS harvest_region varchar(120),
    ADD COLUMN IF NOT EXISTS organic boolean,
    ADD COLUMN IF NOT EXISTS organic_cert_number varchar(120),
    ADD COLUMN IF NOT EXISTS organic_cert_body varchar(120),
    ADD COLUMN IF NOT EXISTS organic_cert_expiry date,
    ADD COLUMN IF NOT EXISTS supplier_name varchar(255),
    ADD COLUMN IF NOT EXISTS supplier_code varchar(120),
    ADD COLUMN IF NOT EXISTS supplier_contact varchar(255),
    ADD COLUMN IF NOT EXISTS olive_source_type varchar(40),
    ADD COLUMN IF NOT EXISTS olive_source_reference varchar(255),
    ADD COLUMN IF NOT EXISTS production_batch_ref varchar(120),
    ADD COLUMN IF NOT EXISTS extraction_batch_ref varchar(120),
    ADD COLUMN IF NOT EXISTS product_status varchar(40),
    ADD COLUMN IF NOT EXISTS nutrition_declaration_json text,
    ADD COLUMN IF NOT EXISTS brand_description text;

COMMENT ON COLUMN skus.ingredient_declaration IS 'Mandatory ingredient list for labeling (e.g. 100% Extra Virgin Olive Oil).';
COMMENT ON COLUMN skus.product_status IS 'Workflow: DRAFT, PENDING_REVIEW, APPROVED, ACTIVE, INACTIVE, ARCHIVED.';
