-- NOTE: seed JSON block below is regenerated from permissions-spec.json via: node oosm/scripts/sync-permissions.cjs
-- NOTE: seed JSON block below is regenerated from permissions-spec.json via: node oosm/scripts/sync-permissions.cjs
-- NOTE: seed JSON block below is regenerated from permissions-spec.json via: node oosm/scripts/sync-permissions.cjs
-- ====== 0) Pré-requis & idempotence ======
CREATE EXTENSION IF NOT EXISTS pgcrypto; -- pour gen_random_uuid()

-- Index d'unicité pour éviter les doublons
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM   pg_indexes
            WHERE  schemaname = 'public'
              AND    indexname = 'uq_permission_mod_entity_name'
        ) THEN
            EXECUTE 'CREATE UNIQUE INDEX uq_permission_mod_entity_name
             ON public.permission(module, entity, permission_name)';
        END IF;
    END;
$$ LANGUAGE plpgsql;

-- ====== 1) Fonction de seed depuis un JSON ======
CREATE OR REPLACE FUNCTION public.seed_permissions_from_json(spec jsonb)
    RETURNS void
    LANGUAGE plpgsql
AS $$
DECLARE
    ent_key  text;     -- nom d'entité (clé du JSON)
    ent_val  jsonb;    -- objet {module, permissions, ...}
    mod_txt  text;     -- "FINANCE" | "RECEPTION" | ...
    mod_int  smallint; -- 0..4
    action   text;     -- "READ" | "CREATE" | ...
    v_created int := 0;
    v_skipped int := 0;
BEGIN
    -- Parcourt spec.entities
    FOR ent_key, ent_val IN
        SELECT key, value
            FROM jsonb_each(COALESCE(spec->'entities', '{}'::jsonb))
            UNION ALL
            SELECT key, value
            FROM jsonb_each(COALESCE(spec->'security_entities', '{}'::jsonb))
        LOOP
            mod_txt := UPPER(trim((ent_val->>'module')));
            -- Mapping texte -> smallint (conforme à OSMModule)
            mod_int :=
                    CASE mod_txt
                        WHEN 'HR'           THEN 0
                        WHEN 'RECEPTION'    THEN 1
                        WHEN 'PRODUCTION'   THEN 2
                        WHEN 'FINANCE'      THEN 3
                        WHEN 'HABILITATION' THEN 4
                        WHEN 'INVENTAIR'     THEN 5
                        WHEN 'CONDITIONING'  THEN 6
                        ELSE NULL
                        END;

            IF mod_int IS NULL THEN
                RAISE WARNING 'Module inconnu "%" pour entité "%", on ignore.', mod_txt, ent_key;
                CONTINUE;
            END IF;

            -- Itère le tableau permissions
            FOR action IN
                SELECT trim(UPPER(value::text), '"')
                FROM jsonb_array_elements_text(ent_val->'permissions')
                LOOP
                    -- Ignore lignes vides
                    IF action IS NULL OR action = '' THEN
                        CONTINUE;
                    END IF;

                    -- Insert idempotent: si (module, entity, permission_name) existe déjà -> DO NOTHING
                    INSERT INTO public.permission
                    (id, created_by, created_date, external_id, is_deleted, last_modified_by, last_modified_date,
                     tenant_id, entity, module, permission_name)
                    VALUES
                        (gen_random_uuid(), NULL, NOW(), gen_random_uuid(), FALSE, NULL, NOW(),
                         NULL, ent_key, mod_int, action)
                    ON CONFLICT (module, entity, permission_name) DO NOTHING;

                    IF FOUND THEN
                        v_created := v_created + 1;
                    ELSE
                        v_skipped := v_skipped + 1;
                    END IF;
                END LOOP;
        END LOOP;

    RAISE NOTICE 'Seed terminé. Créés=%, Ignorés(existaient déjà)=%', v_created, v_skipped;
END;
$$;

-- ====== 1.1) Rename legacy product permission resources ======
DO $$
DECLARE
    legacy_permission RECORD;
    target_permission_id UUID;
BEGIN
    FOR legacy_permission IN
        SELECT id, module, permission_name
        FROM public.permission
        WHERE UPPER(entity) IN ('SKU', 'PRODUCT')
        LOOP
            SELECT id
            INTO target_permission_id
            FROM public.permission
            WHERE module = legacy_permission.module
              AND UPPER(entity) = 'PRODUITFINAL'
              AND permission_name = legacy_permission.permission_name
            LIMIT 1;

            IF target_permission_id IS NULL THEN
                UPDATE public.permission
                SET entity = 'PRODUITFINAL'
                WHERE id = legacy_permission.id;
            ELSE
                INSERT INTO public.role_permissions (role_id, permissions_id)
                SELECT rp.role_id, target_permission_id
                FROM public.role_permissions rp
                WHERE rp.permissions_id = legacy_permission.id
                  AND NOT EXISTS (
                      SELECT 1
                      FROM public.role_permissions existing_rp
                      WHERE existing_rp.role_id = rp.role_id
                        AND existing_rp.permissions_id = target_permission_id
                  );

                DELETE FROM public.role_permissions
                WHERE permissions_id = legacy_permission.id;

                DELETE FROM public.permission
                WHERE id = legacy_permission.id;
            END IF;
        END LOOP;
END;
$$ LANGUAGE plpgsql;

-- ====== 2) Exemple d'appel : colle ton JSON entre $$ ... $$ ======
-- Remplace le contenu par TON fichier "permisisons and modules .json"
SELECT public.seed_permissions_from_json($${
  "entities": {
    "BANKACCOUNT": {
      "description": "Bank account management",
      "module": "FINANCE",
      "permissions": [
        "CREATE",
        "DELETE",
        "READ",
        "UPDATE"
      ]
    },
    "base_type": {
      "description": "Generic type system",
      "module": "PRODUCTION",
      "permissions": [
        "CREATE",
        "DELETE",
        "READ",
        "UPDATE"
      ]
    },
    "EXPENSE": {
      "description": "Expense management",
      "module": "FINANCE",
      "permissions": [
        "APPROVE",
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "PAY",
        "READ",
        "REJECT",
        "UPDATE",
        "VALIDATE"
      ]
    },
    "FINANCIALTRANSACTION": {
      "description": "Universal financial transactions",
      "module": "FINANCE",
      "permissions": [
        "APPROVE",
        "COMPLETE_PAYMENT_DETAILS",
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "PAY",
        "READ",
        "REJECT",
        "UPDATE",
        "VALIDATE"
      ]
    },
    "OILCREDIT": {
      "description": "Oil credit management",
      "module": "PRODUCTION",
      "permissions": [
        "APPROVE",
        "COMPLETE",
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "READ",
        "REJECT",
        "UPDATE",
        "VALIDATE"
      ]
    },
    "SUPPLIER": {
      "description": "Supplier management",
      "module": "RECEPTION",
      "permissions": [
        "ASSIGN_SUPPLIER",
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "READ",
        "UPDATE",
        "VALIDATE"
      ]
    },
    "UNIFIEDDELIVERY": {
      "description": "Delivery management",
      "module": "RECEPTION",
      "permissions": [
        "COMPLETE",
        "COMPLETE_PAYMENT_DETAILS",
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "GEN_PDF_PRODUCTION",
        "GEN_PDF_QC_OIL",
        "GEN_PDF_QC_OLIVE",
        "OIL_QUALITY",
        "OIL_RECEPTION",
        "OLIVE_QUALITY",
        "PAY",
        "PLANNING",
        "READ",
        "SET_PRICE",
        "TO_PROD",
        "UPDATE",
        "UPDATE_OIL_QUALITY",
        "UPDATE_OLIVE_QUALITY",
        "VALIDATE"
      ]
    },
    "MACHINEPLAN": {
      "description": "Machine planning and scheduling",
      "module": "PRODUCTION",
      "permissions": [
        "APPROVE",
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "MAINTENANCE",
        "READ",
        "REJECT",
        "UPDATE",
        "VALIDATE"
      ]
    },
    "MILLMACHINE": {
      "description": "Mill machine management",
      "module": "PRODUCTION",
      "permissions": [
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "MAINTENANCE",
        "READ",
        "UPDATE",
        "VALIDATE"
      ]
    },
    "MAINTENANCEWORKORDER": {
      "description": "Equipment maintenance work orders",
      "module": "PRODUCTION",
      "permissions": [
        "CREATE",
        "DELETE",
        "READ",
        "UPDATE"
      ]
    },
    "OILTRANSACTION": {
      "description": "Oil transaction management",
      "module": "PRODUCTION",
      "permissions": [
        "COMPLETE",
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "OIL_IN_TRANSACTION",
        "OIL_OUT_TRANSACTION",
        "OIL_PAYMENT",
        "READ",
        "UPDATE",
        "VALIDATE"
      ]
    },
    "PARAMETER": {
      "description": "System parameters management",
      "module": "HABILITATION",
      "permissions": [
        "CREATE",
        "DELETE",
        "READ",
        "UPDATE"
      ]
    },
    "QUALITYCONTROLRESULT": {
      "description": "Quality control results",
      "module": "PRODUCTION",
      "permissions": [
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "OIL_QUALITY",
        "OLIVE_QUALITY",
        "READ",
        "UPDATE",
        "UPDATE_OIL_QUALITY",
        "UPDATE_OLIVE_QUALITY",
        "VALIDATE"
      ]
    },
    "QUALITYCONTROLRULE": {
      "description": "Quality control rules",
      "module": "PRODUCTION",
      "permissions": [
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "READ",
        "UPDATE",
        "VALIDATE"
      ]
    },
    "STORAGEUNIT": {
      "description": "Storage unit management",
      "module": "PRODUCTION",
      "permissions": [
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "READ",
        "SET_PRICE",
        "UPDATE",
        "VALIDATE"
      ]
    },
    "TRANSPORTER": {
      "description": "Transporter management",
      "module": "RECEPTION",
      "permissions": [
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "READ",
        "UPDATE",
        "VALIDATE"
      ]
    },
    "OILSALE": {
      "description": "Oil sales management (includes bundled container lines; creates OIL_SALE and OIL_CONTAINER_SALE financial transactions)",
      "module": "FINANCE",
      "permissions": [
        "APPROVE",
        "CANCEL",
        "COMPLETE",
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "PAY",
        "READ",
        "REJECT",
        "UPDATE",
        "VALIDATE"
      ]
    },
    "WASTESALE": {
      "description": "Waste sales management (finance module — frontend routes and menus)",
      "module": "FINANCE",
      "permissions": [
        "CANCEL",
        "COMPLETE",
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "PAY",
        "READ",
        "UPDATE",
        "VALIDATE"
      ]
    },
    "WASTE": {
      "description": "Waste records and payments (production API resource — alias synced with WASTESALE)",
      "module": "PRODUCTION",
      "permissions": [
        "COMPLETE",
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "PAY",
        "READ",
        "UPDATE",
        "VALIDATE"
      ]
    },
    "OILCONTAINER": {
      "description": "Oil container catalog, stock and purchases (uses MaterielSupplier for purchase supplier)",
      "module": "PRODUCTION",
      "permissions": [
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "READ",
        "UPDATE",
        "VALIDATE"
      ]
    },
    "FILTRATIONOPERATION": {
      "description": "Oil filtration operations",
      "module": "PRODUCTION",
      "permissions": [
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "READ",
        "UPDATE",
        "VALIDATE"
      ]
    },
    "CONTRACT": {
      "description": "Employee contracts",
      "module": "HR",
      "permissions": [
        "CREATE",
        "DELETE",
        "READ",
        "UPDATE"
      ]
    },
    "DEPARTMENT": {
      "description": "Departments",
      "module": "HR",
      "permissions": [
        "CREATE",
        "DELETE",
        "READ",
        "UPDATE"
      ]
    },
    "EMPLOYEE": {
      "description": "Employee profiles",
      "module": "HR",
      "permissions": [
        "CREATE",
        "DELETE",
        "READ",
        "UPDATE"
      ]
    },
    "LEAVEREQUEST": {
      "description": "Employee leave requests",
      "module": "HR",
      "permissions": [
        "CREATE",
        "DELETE",
        "READ",
        "UPDATE"
      ]
    },
    "PAYROLL": {
      "description": "Payroll runs and items",
      "module": "HR",
      "permissions": [
        "CREATE",
        "DELETE",
        "READ",
        "UPDATE"
      ]
    },
    "POINTAGE": {
      "description": "Time clock entries",
      "module": "HR",
      "permissions": [
        "CREATE",
        "DELETE",
        "READ",
        "UPDATE"
      ]
    },
    "POSTE": {
      "description": "Job positions",
      "module": "HR",
      "permissions": [
        "CREATE",
        "DELETE",
        "READ",
        "UPDATE"
      ]
    }
  },
  "security_entities": {
    "COMPANYPROFILE": {
      "description": "Company profile management",
      "module": "HABILITATION",
      "permissions": [
        "CREATE",
        "DELETE",
        "READ",
        "UPDATE"
      ]
    },
    "ROLE": {
      "description": "Role management",
      "module": "HABILITATION",
      "permissions": [
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "READ",
        "UPDATE"
      ]
    },
    "PERMISSION": {
      "description": "Permission management",
      "module": "HABILITATION",
      "permissions": [
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "READ",
        "UPDATE"
      ]
    },
    "OOSMUSER": {
      "description": "User management",
      "module": "HABILITATION",
      "permissions": [
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "READ",
        "UPDATE"
      ]
    },
    "CLIENT": {
      "description": "Conditioning clients",
      "module": "CONDITIONING",
      "permissions": [
        "CREATE",
        "DELETE",
        "READ",
        "UPDATE"
      ]
    },
    "ARTICLESEC": {
      "description": "Article management (frontend routes and menus)",
      "module": "INVENTAIR",
      "permissions": [
        "CREATE",
        "DELETE",
        "ENTREE_STOCK",
        "READ",
        "SORTIE_STOCK",
        "UPDATE"
      ]
    },
    "ARTICLE": {
      "description": "Article management API resource (ArticleSecController — alias synced with ARTICLESEC)",
      "module": "INVENTAIR",
      "permissions": [
        "CREATE",
        "DELETE",
        "ENTREE_STOCK",
        "READ",
        "SORTIE_STOCK",
        "UPDATE"
      ]
    },
    "BOM": {
      "description": "Bill of materials",
      "module": "INVENTAIR",
      "permissions": [
        "CREATE",
        "DELETE",
        "READ",
        "UPDATE"
      ]
    },
    "BONCOMMANDE": {
      "description": "Purchase order",
      "module": "INVENTAIR",
      "permissions": [
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "READ",
        "UPDATE",
        "VALIDATE"
      ]
    },
    "EMPLACEMENTSTOCK": {
      "description": "Stock location",
      "module": "INVENTAIR",
      "permissions": [
        "ASSIGN_EMPLACEMENT",
        "CREATE",
        "DELETE",
        "LIBERER_STOCK",
        "READ",
        "RESERVER_STOCK",
        "TRANSFERER_STOCK",
        "UPDATE"
      ]
    },
    "MATERIEL_SUPPLIER": {
      "description": "Material / packaging supplier management (inventory stock — distinct from RECEPTION:SUPPLIER olive/oil farmers)",
      "module": "INVENTAIR",
      "permissions": [
        "CREATE",
        "DELETE",
        "READ",
        "UPDATE"
      ]
    },
    "FOURNISSEUR": {
      "description": "Legacy alias — migrated to MATERIEL_SUPPLIER",
      "module": "INVENTAIR",
      "permissions": [
        "CREATE",
        "DELETE",
        "READ",
        "UPDATE"
      ]
    },
    "LIGNEBONCOMMANDE": {
      "description": "Purchase order line",
      "module": "INVENTAIR",
      "permissions": [
        "CREATE",
        "DELETE",
        "READ",
        "UPDATE"
      ]
    },
    "LIGNECONDITIONNEMENT": {
      "description": "Packaging line",
      "module": "INVENTAIR",
      "permissions": [
        "CREATE",
        "DELETE",
        "READ",
        "UPDATE"
      ]
    },
    "MOUVEMENTSTOCKSEC": {
      "description": "Stock movement",
      "module": "INVENTAIR",
      "permissions": [
        "CREATE",
        "DELETE",
        "READ",
        "UPDATE"
      ]
    },
    "PRODUITFINAL": {
      "description": "Finished product",
      "module": "INVENTAIR",
      "permissions": [
        "CREATE",
        "DELETE",
        "READ",
        "UPDATE"
      ]
    },
    "STOCKSEC": {
      "description": "Stock management",
      "module": "INVENTAIR",
      "permissions": [
        "AJUSTER_STOCK",
        "ASSIGN_EMPLACEMENT",
        "CHECK_STOCK",
        "CREATE",
        "DELETE",
        "ENTREE_STOCK",
        "LIBERER_STOCK",
        "READ",
        "RESERVER_STOCK",
        "SORTIE_STOCK",
        "TRANSFERER_STOCK",
        "UPDATE"
      ]
    },
    "OF": {
      "description": "Conditioning manufacturing orders",
      "module": "CONDITIONING",
      "permissions": [
        "AJUSTER_STOCK",
        "CLOSE",
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "PAUSE",
        "READ",
        "RESUME",
        "START",
        "UPDATE"
      ]
    },
    "PROJET": {
      "description": "Conditioning projects",
      "module": "CONDITIONING",
      "permissions": [
        "CANCEL",
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "READ",
        "UPDATE",
        "UPDATE_STATUS"
      ]
    },
    "CERTIFICATION": {
      "description": "Conditioning certifications",
      "module": "CONDITIONING",
      "permissions": [
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "READ",
        "UPDATE",
        "VALIDATE"
      ]
    },
    "SHIPPING": {
      "description": "Conditioning shipping information",
      "module": "CONDITIONING",
      "permissions": [
        "ADD_LINE",
        "CREATE",
        "DELETE",
        "DELIVER",
        "GEN_PDF",
        "READ",
        "REMOVE_LINE",
        "SHIP",
        "UPDATE",
        "UPDATE_STATUS"
      ]
    },
    "EXPEDITION": {
      "description": "Conditioning expedition management",
      "module": "CONDITIONING",
      "permissions": [
        "ADD_LINE",
        "CANCEL",
        "CLOSE",
        "CREATE",
        "DELETE",
        "DELIVER",
        "GEN_PDF",
        "READ",
        "REMOVE_LINE",
        "SHIP",
        "UPDATE",
        "VALIDATE"
      ]
    },
    "QUALITY": {
      "description": "Conditioning quality control",
      "module": "CONDITIONING",
      "permissions": [
        "CREATE",
        "DELETE",
        "GEN_PDF",
        "READ",
        "UPDATE",
        "UPDATE_STATUS",
        "VALIDATE"
      ]
    },
    "LABELCONTENT": {
      "description": "Conditioning label content",
      "module": "CONDITIONING",
      "permissions": [
        "CREATE",
        "DELETE",
        "DRAFT",
        "EXPORT",
        "FINALIZE",
        "GEN_PDF",
        "READ",
        "UPDATE",
        "VALIDATE"
      ]
    },
    "ANALYTICS": {
      "description": "Conditioning analytics and reports",
      "module": "CONDITIONING",
      "permissions": [
        "EXPORT",
        "GEN_PDF",
        "READ",
        "REPORT"
      ]
    },
    "MOBILESYNC": {
      "description": "Conditioning mobile synchronization",
      "module": "CONDITIONING",
      "permissions": [
        "READ",
        "SYNC"
      ]
    },
    "AUDIT": {
      "description": "Conditioning audit logs",
      "module": "CONDITIONING",
      "permissions": [
        "READ"
      ]
    }
  },
  "modules": {
    "HR": {
      "value": 0,
      "description": "Human Resources module"
    },
    "RECEPTION": {
      "value": 1,
      "description": "Reception and supplier management"
    },
    "PRODUCTION": {
      "value": 2,
      "description": "Production and quality control"
    },
    "FINANCE": {
      "value": 3,
      "description": "Financial management"
    },
    "HABILITATION": {
      "value": 4,
      "description": "System administration and configuration"
    },
    "INVENTAIR": {
      "value": 5,
      "description": "Inventory and packaging stock management"
    },
    "CONDITIONING": {
      "value": 6,
      "description": "Conditioning, orders, projects, labels and expedition management"
    }
  },
  "actions": {
    "READ": "View entity records",
    "CREATE": "Create new entity records",
    "UPDATE": "Modify existing entity records",
    "DELETE": "Remove entity records",
    "CANCEL": "Cancel operations or transactions",
    "OLIVE_QUALITY": "Manage olive quality control",
    "OIL_QUALITY": "Manage oil quality control",
    "UPDATE_OLIVE_QUALITY": "Update olive quality parameters",
    "UPDATE_OIL_QUALITY": "Update oil quality parameters",
    "TO_PROD": "Move to production workflow",
    "COMPLETE": "Complete operations or transactions",
    "OIL_PAYMENT": "Process oil-related payments",
    "OIL_OUT_TRANSACTION": "Process oil output transactions",
    "OIL_IN_TRANSACTION": "Process oil input transactions",
    "OIL_RECEPTION": "Manage oil reception processes",
    "SET_PRICE": "Set pricing for entities",
    "ASSIGN_SUPPLIER": "Assign suppliers to operations",
    "COMPLETE_PAYMENT_DETAILS": "Complete payment information",
    "VALIDATE": "Validate operations or data",
    "PAY": "Process payments",
    "GEN_PDF": "Generate PDF documents",
    "APPROVE": "Approve operations or requests",
    "REJECT": "Reject operations or requests",
    "MAINTENANCE": "Perform maintenance operations",
    "PLANNING": "Manage planning and scheduling",
    "DELIVERYHISTORY": "Manage the history of deliveries",
    "START": "Start workflow execution",
    "PAUSE": "Pause workflow execution",
    "RESUME": "Resume workflow execution",
    "CLOSE": "Close workflow execution",
    "SHIP": "Ship expedition or delivery",
    "DELIVER": "Mark expedition or delivery as delivered",
    "ADD_LINE": "Add lines to a document or operation",
    "REMOVE_LINE": "Remove lines from a document or operation",
    "UPDATE_STATUS": "Update business status",
    "DRAFT": "Move content back to draft",
    "FINALIZE": "Finalize content",
    "EXPORT": "Export content or reports",
    "SYNC": "Synchronize mobile/offline data",
    "REPORT": "Generate analytical reports",
    "ENTREE_STOCK": "",
    "SORTIE_STOCK": "",
    "AJUSTER_STOCK": "",
    "ASSIGN_EMPLACEMENT": "",
    "RESERVER_STOCK": "",
    "LIBERER_STOCK": "",
    "CHECK_STOCK": "",
    "TRANSFERER_STOCK": ""
  }
}$$::jsonb);

-- ====== 1.2) Migrate legacy FOURNISSEUR -> MATERIEL_SUPPLIER ======
DO $$
DECLARE
    legacy_permission RECORD;
    target_permission_id UUID;
BEGIN
    FOR legacy_permission IN
        SELECT id, module, permission_name
        FROM public.permission
        WHERE UPPER(entity) = 'FOURNISSEUR'
        LOOP
            SELECT id
            INTO target_permission_id
            FROM public.permission
            WHERE module = legacy_permission.module
              AND UPPER(entity) = 'MATERIEL_SUPPLIER'
              AND permission_name = legacy_permission.permission_name
            LIMIT 1;

            IF target_permission_id IS NULL THEN
                UPDATE public.permission
                SET entity = 'MATERIEL_SUPPLIER'
                WHERE id = legacy_permission.id;
            ELSE
                INSERT INTO public.role_permissions (role_id, permissions_id)
                SELECT rp.role_id, target_permission_id
                FROM public.role_permissions rp
                WHERE rp.permissions_id = legacy_permission.id
                  AND NOT EXISTS (
                      SELECT 1
                      FROM public.role_permissions existing_rp
                      WHERE existing_rp.role_id = rp.role_id
                        AND existing_rp.permissions_id = target_permission_id
                  );

                DELETE FROM public.role_permissions
                WHERE permissions_id = legacy_permission.id;

                DELETE FROM public.permission
                WHERE id = legacy_permission.id;
            END IF;
        END LOOP;
END;
$$ LANGUAGE plpgsql;

-- ====== 1.3) Mirror role grants onto backend/API alias entities ======
DO $$
BEGIN
    -- ARTICLESEC (FE) -> ARTICLE (ArticleSecController)
    INSERT INTO public.role_permissions (role_id, permissions_id)
    SELECT rp.role_id, target_perm.id
    FROM public.role_permissions rp
             JOIN public.permission source_perm ON source_perm.id = rp.permissions_id
             JOIN public.permission target_perm
                  ON target_perm.module = source_perm.module
                      AND UPPER(target_perm.entity) = 'ARTICLE'
                      AND target_perm.permission_name = source_perm.permission_name
    WHERE UPPER(source_perm.entity) = 'ARTICLESEC'
      AND NOT EXISTS (
        SELECT 1
        FROM public.role_permissions existing_rp
        WHERE existing_rp.role_id = rp.role_id
          AND existing_rp.permissions_id = target_perm.id
    );

    -- FINANCE:WASTESALE (FE) -> PRODUCTION:WASTE (WasteController)
    INSERT INTO public.role_permissions (role_id, permissions_id)
    SELECT rp.role_id, target_perm.id
    FROM public.role_permissions rp
             JOIN public.permission source_perm ON source_perm.id = rp.permissions_id
             JOIN public.permission target_perm
                  ON UPPER(target_perm.entity) = 'WASTE'
                      AND target_perm.permission_name = source_perm.permission_name
    WHERE source_perm.module = 3
      AND UPPER(source_perm.entity) = 'WASTESALE'
      AND NOT EXISTS (
        SELECT 1
        FROM public.role_permissions existing_rp
        WHERE existing_rp.role_id = rp.role_id
          AND existing_rp.permissions_id = target_perm.id
    );

    -- PRODUCTION:STORAGEUNIT -> PRODUCTION:OILCONTAINER (container API)
    INSERT INTO public.role_permissions (role_id, permissions_id)
    SELECT rp.role_id, target_perm.id
    FROM public.role_permissions rp
             JOIN public.permission source_perm ON source_perm.id = rp.permissions_id
             JOIN public.permission target_perm
                  ON target_perm.module = source_perm.module
                      AND UPPER(target_perm.entity) = 'OILCONTAINER'
                      AND target_perm.permission_name = source_perm.permission_name
    WHERE source_perm.module = 2
      AND UPPER(source_perm.entity) = 'STORAGEUNIT'
      AND NOT EXISTS (
        SELECT 1
        FROM public.role_permissions existing_rp
        WHERE existing_rp.role_id = rp.role_id
          AND existing_rp.permissions_id = target_perm.id
    );
END;
$$ LANGUAGE plpgsql;
