# Railway Database Bootstrap

Use `update`, not `create` or `create-drop`.

```text
HIBERNATE_DDL_AUTO=update
```

Do not set:

```text
HIBERNATE_DDL_AUTO=create
HIBERNATE_DDL_AUTO=create-drop
```

## Order

1. Create the Railway PostgreSQL service.
2. Set backend DB variables.
3. Deploy backend once with `HIBERNATE_DDL_AUTO=update`.
4. Wait until Hibernate creates the base tables.
5. Run the manual SQL scripts.
6. Restart/redeploy the backend service.

## Variables

Use either explicit JDBC variables:

```text
DB_URL=jdbc:postgresql://<host>:<port>/<database>
DB_USER=<user>
DB_PASS=<password>
```

or Railway PostgreSQL variables:

```text
PGHOST=<host>
PGPORT=<port>
PGDATABASE=<database>
PGUSER=<user>
PGPASSWORD=<password>
```

## Script Runner

Run idempotent scripts:

```powershell
.\scripts\Run-RailwayDatabaseScripts.ps1
```

On a fresh database only, include one-time seed scripts:

```powershell
.\scripts\Run-RailwayDatabaseScripts.ps1 -IncludeOneTimeSeeds
```

The one-time QC rules seed is not idempotent and can duplicate rows if rerun.

## Manual Script Order

```text
modules/production/src/main/resources/legacy/osm-prod/db/migration/20260522_add_traceability_lot_table.sql
modules/production/src/main/resources/legacy/osm-prod/db/migration/20260522_add_filtration_quality_control_links.sql
modules/conditioning/src/main/resources/legacy/osm-cond/db/migration/20260522_add_traceability_lot_columns.sql
modules/inventory/src/main/resources/legacy/osm-pack/db/migration/20260511_add_product_olive_oil_attributes.sql
modules/conditioning/src/main/resources/legacy/osm-cond/db/migration/20260517_add_product_relation_to_labels.sql
modules/inventory/src/main/resources/legacy/osm-pack/db/migration/V20260523__stock_bom_enhancements.sql
modules/production/src/main/resources/legacy/osm-prod/db/migration/20260517_seed_production_parameters.sql
modules/production/src/main/resources/legacy/osm-prod/db/migration/insert param.sql
modules/production/src/main/resources/legacy/osm-prod/db/migration/insert Permissions.sql
modules/production/src/main/resources/legacy/osm-prod/db/migration/insert qc rules.sql
```
