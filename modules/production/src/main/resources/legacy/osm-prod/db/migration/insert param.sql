UPDATE public.parameter
SET is_deleted = false
WHERE code IN ('OLIVE_UNIT_PRICE', 'DAILY_OIL_METRIC', 'PRIX_TRITURATION_KG')
  AND tenant_id = '4b322fea-6825-4c4c-9534-021cd150d112'
  AND is_deleted IS NULL;

WITH tenant_ids AS (SELECT DISTINCT tenant_id
                    FROM parameter
                    WHERE tenant_id IS NOT NULL
                    UNION
                    SELECT '4b322fea-6825-4c4c-9534-021cd150d112'::uuid)
INSERT
INTO parameter
(id, created_by, created_date, tenant_id, category, code, description, is_active, type, value)
SELECT gen_random_uuid(),
       'system',
       NOW(),
       tenant_id,
       'FINANCE',
       'PRIX_TRITURATION_KG',
       'Milling price per kg (TND)',
       TRUE,
       'DOUBLE',
       '0'
FROM tenant_ids t
WHERE NOT EXISTS (SELECT 1
                  FROM parameter p
                  WHERE p.tenant_id = t.tenant_id
                    AND p.code = 'PRIX_TRITURATION_KG');
