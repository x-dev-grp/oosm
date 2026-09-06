-- =============================================================================
-- Clean ONE tenant's BUSINESS DATA (keep the tenant itself)
-- Keeps: company_profile, oosmuser, tenant_enabled_module, roles/permissions,
--        oauth2_* clients, app_setting*, flyway history, and parameter (config).
-- Deletes: every other public table row with that tenant_id (FK-safe multi-pass),
--          then breaks leftover FKs that still point at this tenant's rows
--          (e.g. child rows with null/other tenant_id).
-- =============================================================================
-- Edit v_tenant below, then run the whole script (DataGrip / DBeaver / psql).
-- =============================================================================

DO $$
DECLARE
  -- >>> SET YOUR TENANT UUID HERE <<<
  v_tenant uuid := 'ba25db13-13e3-42b9-a965-06e5c9a0f31f';

  v_legal  text;
  v_table  text;
  v_sql    text;
  v_deleted int;
  v_total  int := 0;
  v_round  int;
  v_progress int;
  v_targets text[];
  v_remaining text[];
  v_next text[];
  v_max_rounds int;
  v_ref RECORD;
  v_keep text[] := ARRAY[
    'company_profile',
    'oosmuser',
    'tenant_enabled_module',
    'oauth2_registered_client',
    'oauth2_authorization',
    'oauth2_authorization_consent',
    'role',
    'permission',
    'roles_permissions',
    'app_setting',
    'app_setting_audit',
    'flyway_schema_history',
    'flyway_schema_history_finance',
    'flyway_schema_history_hr',
    'flyway_schema_history_production',
    'flyway_schema_history_security',
    'flyway_schema_history_shared',
    'parameter'
  ];
BEGIN
  IF v_tenant = '00000000-0000-0000-0000-000000000000'::uuid THEN
    RAISE EXCEPTION 'Edit v_tenant at the top of the script with your real company/tenant UUID';
  END IF;

  IF NOT EXISTS (SELECT 1 FROM company_profile WHERE id = v_tenant) THEN
    RAISE EXCEPTION 'No company_profile for tenant %', v_tenant;
  END IF;

  SELECT legal_name INTO v_legal FROM company_profile WHERE id = v_tenant;
  RAISE NOTICE 'Tenant % (%) — wiping business tables with tenant_id', v_tenant, COALESCE(v_legal, '?');

  SELECT coalesce(array_agg(c.table_name ORDER BY c.table_name), ARRAY[]::text[])
  INTO v_targets
  FROM information_schema.columns c
  JOIN information_schema.tables t
    ON t.table_schema = c.table_schema AND t.table_name = c.table_name
  WHERE c.table_schema = 'public'
    AND c.column_name = 'tenant_id'
    AND t.table_type = 'BASE TABLE'
    AND NOT (lower(c.table_name) = ANY (SELECT lower(x) FROM unnest(v_keep) AS x));

  IF coalesce(array_length(v_targets, 1), 0) = 0 THEN
    RAISE NOTICE 'No wipeable tables found.';
    RETURN;
  END IF;

  RAISE NOTICE 'Target tables: %', array_length(v_targets, 1);
  v_remaining := v_targets;
  v_max_rounds := greatest(8, coalesce(array_length(v_targets, 1), 0) + 2);

  -- Pass 1: DELETE WHERE tenant_id = v_tenant (retry on FK order)
  FOR v_round IN 0 .. v_max_rounds LOOP
    EXIT WHEN coalesce(array_length(v_remaining, 1), 0) = 0;
    v_progress := 0;
    v_next := ARRAY[]::text[];

    FOREACH v_table IN ARRAY v_remaining LOOP
      BEGIN
        v_sql := format('DELETE FROM %I WHERE tenant_id = $1', v_table);
        EXECUTE v_sql USING v_tenant;
        GET DIAGNOSTICS v_deleted = ROW_COUNT;
        v_total := v_total + v_deleted;
        v_progress := v_progress + 1;
        IF v_deleted > 0 THEN
          RAISE NOTICE '[tenant_id round %] DELETE % : % rows', v_round, v_table, v_deleted;
        END IF;
      EXCEPTION
        WHEN foreign_key_violation THEN
          v_next := array_append(v_next, v_table);
        WHEN OTHERS THEN
          RAISE NOTICE '[tenant_id round %] defer % : %', v_round, v_table, SQLERRM;
          v_next := array_append(v_next, v_table);
      END;
    END LOOP;

    IF v_progress = 0 THEN
      EXIT; -- move to FK-break pass
    END IF;

    v_remaining := v_next;
  END LOOP;

  -- Pass 2: for leftover masters, delete ANY referencing rows that point at
  -- this tenant's PKs (covers null/other tenant_id children), then retry DELETE.
  FOR v_round IN 0 .. v_max_rounds LOOP
    EXIT WHEN coalesce(array_length(v_remaining, 1), 0) = 0;
    v_progress := 0;
    v_next := ARRAY[]::text[];

    FOREACH v_table IN ARRAY v_remaining LOOP
      -- Break inbound FKs first
      FOR v_ref IN
        SELECT
          tc.table_name AS referencing_table,
          kcu.column_name AS referencing_column,
          ccu.column_name AS referenced_column
        FROM information_schema.table_constraints tc
        JOIN information_schema.key_column_usage kcu
          ON tc.constraint_name = kcu.constraint_name
         AND tc.table_schema = kcu.table_schema
        JOIN information_schema.constraint_column_usage ccu
          ON ccu.constraint_name = tc.constraint_name
         AND ccu.table_schema = tc.table_schema
        WHERE tc.constraint_type = 'FOREIGN KEY'
          AND tc.table_schema = 'public'
          AND ccu.table_name = v_table
      LOOP
        BEGIN
          -- Never wipe identity/config tables via this FK-break path
          IF lower(v_ref.referencing_table) = ANY (SELECT lower(x) FROM unnest(v_keep) AS x) THEN
            CONTINUE;
          END IF;

          v_sql := format(
            'DELETE FROM %I WHERE %I IN (SELECT %I FROM %I WHERE tenant_id = $1)',
            v_ref.referencing_table,
            v_ref.referencing_column,
            v_ref.referenced_column,
            v_table
          );
          EXECUTE v_sql USING v_tenant;
          GET DIAGNOSTICS v_deleted = ROW_COUNT;
          IF v_deleted > 0 THEN
            v_total := v_total + v_deleted;
            RAISE NOTICE '[fk-break] DELETE % via %.% : % rows',
              v_ref.referencing_table, v_table, v_ref.referencing_column, v_deleted;
          END IF;
        EXCEPTION
          WHEN OTHERS THEN
            RAISE NOTICE '[fk-break] defer % -> % : %',
              v_ref.referencing_table, v_table, SQLERRM;
        END;
      END LOOP;

      -- Retry master delete
      BEGIN
        v_sql := format('DELETE FROM %I WHERE tenant_id = $1', v_table);
        EXECUTE v_sql USING v_tenant;
        GET DIAGNOSTICS v_deleted = ROW_COUNT;
        v_total := v_total + v_deleted;
        v_progress := v_progress + 1;
        RAISE NOTICE '[fk-break round %] DELETE % : % rows', v_round, v_table, v_deleted;
      EXCEPTION
        WHEN foreign_key_violation THEN
          v_next := array_append(v_next, v_table);
        WHEN OTHERS THEN
          RAISE NOTICE '[fk-break round %] still blocked % : %', v_round, v_table, SQLERRM;
          v_next := array_append(v_next, v_table);
      END;
    END LOOP;

    IF v_progress = 0 AND coalesce(array_length(v_next, 1), 0) > 0 THEN
      RAISE EXCEPTION 'FK blockers remain for tables: %', v_next;
    END IF;

    v_remaining := v_next;
  END LOOP;

  IF coalesce(array_length(v_remaining, 1), 0) > 0 THEN
    RAISE EXCEPTION 'Leftover tables: %', v_remaining;
  END IF;

  BEGIN
    DELETE FROM "authorization" a
    WHERE a.principal_name IN (
      SELECT u.username FROM oosmuser u
      WHERE u.tenant_id = v_tenant AND u.username IS NOT NULL
    );
    GET DIAGNOSTICS v_deleted = ROW_COUNT;
    RAISE NOTICE 'oauth2_authorization sessions removed: %', v_deleted;
  EXCEPTION
    WHEN undefined_table THEN
      RAISE NOTICE 'oauth2_authorization not present — skipped';
  END;

  -- Uncomment to also wipe tenant parameters:
  -- DELETE FROM parameter WHERE tenant_id = v_tenant;

  RAISE NOTICE 'Done. Approx rows deleted: %', v_total;
  RAISE NOTICE 'Kept company_profile, users, modules, roles/permissions, parameter.';
END $$;
