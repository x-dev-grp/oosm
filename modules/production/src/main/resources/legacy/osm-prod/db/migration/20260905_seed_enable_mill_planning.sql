WITH tenant_ids AS (
    SELECT DISTINCT tenant_id
    FROM parameter
    WHERE tenant_id IS NOT NULL
    UNION
    SELECT '4b322fea-6825-4c4c-9534-021cd150d112'::uuid
)
INSERT INTO parameter
(id, created_by, created_date, last_modified_by, last_modified_date, is_deleted,
 tenant_id, category, code, description, is_active, type, value)
SELECT
    gen_random_uuid(),
    'system',
    NOW(),
    'system',
    NOW(),
    FALSE,
    tenant_id,
    'RECEPTION',
    'ENABLE_MILL_PLANNING',
    'Enable kanban mill planning board. When false, assign mill manually when completing a lot',
    TRUE,
    'BOOLEAN',
    'true'
FROM tenant_ids t
WHERE NOT EXISTS (
    SELECT 1
    FROM parameter p
    WHERE p.tenant_id = t.tenant_id
      AND p.code = 'ENABLE_MILL_PLANNING'
      AND COALESCE(p.is_deleted, FALSE) = FALSE
);
