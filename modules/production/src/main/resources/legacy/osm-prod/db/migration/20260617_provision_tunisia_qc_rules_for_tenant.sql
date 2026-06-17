-- Tunisia default QC rules for ONE tenant (idempotent).
-- 1) Replace YOUR_TENANT_UUID below with your company/tenant id.
-- 2) Run once. Skips rules that already exist (same tenant + rule_key + oil_qc).

-- Optional: see what you already have
-- SELECT rule_key, rule_name, oil_qc, min_value, max_value
-- FROM quality_control_rule
-- WHERE tenant_id = 'YOUR_TENANT_UUID'::uuid AND is_deleted = false
-- ORDER BY oil_qc DESC, rule_key;

INSERT INTO quality_control_rule (
    id, created_date, last_modified_date, is_deleted, tenant_id,
    rule_key, rule_name, rule_type, oil_qc, min_value, max_value, rule_text_value, description, external_id
)
SELECT
    gen_random_uuid(), NOW(), NOW(), false, 'YOUR_TENANT_UUID'::uuid,
    v.rule_key, v.rule_name, v.rule_type, v.oil_qc, v.min_value, v.max_value, v.rule_text_value, v.description, gen_random_uuid()
FROM (VALUES
    -- Oil QC (oil_qc = true)
    ('Categorie',       'Catégorie',                    'STRING',  true,  NULL::real, NULL::real, 'Extra Vierge,Vierge,Lampante', 'Tunisia default — COI/Tunisia — classification huile d''olive vierge'),
    ('Acidite',         'Acidité (% maaa)',             'NUMERIC', true,  0,          2.0,        NULL,                           'Tunisia default — COI/Tunisia — EVOO ≤0.8%, Vierge ≤2%'),
    ('K232',            'K232',                         'NUMERIC', true,  0,          2.60,       NULL,                           'Tunisia default — COI/Tunisia — EVOO ≤2.50, Vierge ≤2.60'),
    ('K270',            'K270',                         'NUMERIC', true,  0,          0.25,       NULL,                           'Tunisia default — COI/Tunisia — EVOO ≤0.22, Vierge ≤0.25'),
    ('DeltaK',          'Delta K',                      'NUMERIC', true,  0,          0.01,       NULL,                           'Tunisia default — COI/Tunisia — Delta K ≤0.01'),
    ('IndicePreoxyde',  'Indice peroxyde (meq O2/kg)',  'NUMERIC', true,  0,          20,         NULL,                           'Tunisia default — COI/Tunisia — indice de peroxyde ≤20'),
    -- Olive QC (oil_qc = false)
    ('Infestees',       'Infestées %',                  'NUMERIC', false, 0,          100,        NULL,                           'Tunisia default — olives infestées (%)'),
    ('Fermentees',      'Fermentées %',                 'NUMERIC', false, 0,          100,        NULL,                           'Tunisia default — olives fermentées (%)'),
    ('Endommagees',     'Endommagées %',                'NUMERIC', false, 0,          100,        NULL,                           'Tunisia default — olives endommagées (%)'),
    ('Categorie',       'Catégorie Olive',              'STRING',  false, NULL::real, NULL::real, 'Vierge Extra,Vierge,Lampante', 'Tunisia default — catégorie olives à réception')
) AS v(rule_key, rule_name, rule_type, oil_qc, min_value, max_value, rule_text_value, description)
WHERE NOT EXISTS (
    SELECT 1
    FROM quality_control_rule r
    WHERE r.tenant_id = 'YOUR_TENANT_UUID'::uuid
      AND r.is_deleted = false
      AND r.rule_key = v.rule_key
      AND COALESCE(r.oil_qc, false) = v.oil_qc
);
