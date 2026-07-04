# OOSM Permission Catalog

Single source of truth for which `(module, entity, action)` permissions exist in OOSM.

## Files

| File | Role |
|------|------|
| `modules/security/src/main/resources/permissions/permissions-spec.json` | **Edit this** — entities, action profiles, role mirrors |
| `modules/security/src/main/resources/permissions/permissions-seed.generated.json` | Generated seed payload (do not edit) |
| `scripts/sync-permissions.cjs` | Generator + validator |
| `modules/production/.../insert Permissions.sql` | Legacy SQL runner — seed JSON block is regenerated |
| `osm-ms-fe/src/app/theme/types/permissions.generated.ts` | Generated FE entity enums |

## Add a new entity

1. Open `permissions-spec.json` and add:

```json
"MY_NEW_ENTITY": {
  "module": "INVENTAIR",
  "description": "Short description",
  "section": "security_entities",
  "profile": "CRUD"
}
```

2. Pick a profile from `actionProfiles` (or use explicit `"permissions": ["READ", "CREATE"]`).

3. Run:

```bash
node oosm/scripts/sync-permissions.cjs
```

4. Apply to database — either:
   - set `PERMISSIONS_SYNC_ON_STARTUP=true` and restart the app, or
   - re-run `insert Permissions.sql` on the security database

## Add a new action to one entity

Option A — only that entity:

```json
"OILSALE": {
  "module": "FINANCE",
  "profile": "FINANCE_SALE",
  "addActions": ["NEW_ACTION"]
}
```

Option B — every entity using a profile: add the action to the profile in `actionProfiles`.

Option C — remove an inherited action:

```json
"removeActions": ["DELETE"]
```

## Action profiles

Profiles are reusable action sets (`CRUD`, `FINANCE_SALE`, `STOCK_FULL`, `OIL_TRANSACTION`, …).
Define once in `actionProfiles`, reference with `"profile": "CRUD"` on entities.

## Backend aliases & mirrors

- `legacyAliases` — old entity names merged into the canonical entity (e.g. `FOURNISSEUR` → `MATERIEL_SUPPLIER`)
- `roleMirrors` — copy role grants between related entities (e.g. `ARTICLESEC` → `ARTICLE`)

Both are applied by `PermissionCatalogSyncService` at startup when sync is enabled.

## HR action profiles

| Profile | Entity | Notable actions beyond CRUD |
|---------|--------|----------------------------|
| `HR_MASTER` | POSTE | EXPORT |
| `HR_EMPLOYEE` | EMPLOYEE | EXPORT, GEN_PDF |
| `HR_CONTRACT` | CONTRACT | GEN_PDF, UPDATE_STATUS |
| `HR_ATTENDANCE` | POINTAGE | GEN_PDF, EXPORT |
| `HR_LEAVE` | LEAVEREQUEST | APPROVE, REJECT, CANCEL, GEN_PDF |
| `HR_PAYROLL_PERIOD` | PAYROLLPERIOD | CALCULATE, VALIDATE, PAY, CLOSE, EXPORT, REPORT |
| `HR_PAYSLIP` | PAYSLIP | CALCULATE, VALIDATE, PAY, GEN_PDF, EXPORT |

## HR role presets (reference)

`rolePresets` in `permissions-spec.json` documents suggested grants for **HR_CLERK**, **HR_MANAGER**, **HR_PAYROLL_OFFICER**, and **HR_ADMIN**. These are not auto-created as roles — use them when configuring roles in User management.

## Commands

```bash
# Bootstrap spec from legacy SQL (first time only)
node oosm/scripts/sync-permissions.cjs --extract

# Regenerate SQL seed + FE types + validate
node oosm/scripts/sync-permissions.cjs

# Validate only (CI)
node oosm/scripts/sync-permissions.cjs --check
```

## Runtime sync (Java)

When `app.security.permissions.sync-on-startup=true` (`PERMISSIONS_SYNC_ON_STARTUP` env var):

- `PermissionCatalogSyncRunner` loads `permissions-spec.json`
- Creates missing `permission` rows idempotently
- **Reactivates** soft-deleted rows that match the catalog
- Merges legacy aliases and role mirrors (legacy rows are soft-deleted only when the canonical permission exists)

Manual trigger (OOSM admin only):

```http
GET /api/security/permission/catalog-status
POST /api/security/permission/sync-catalog
Authorization: Bearer <OOSMADMIN token>
```

Administration UI: **Administration → Permission catalog** (`/administration/permission-catalog`).

## Troubleshooting empty `fetchAll` / empty permission table

The app **auto-seeds on startup when the permission table is empty** (reads `permissions-spec.json`).

If the table is still empty after restart:

1. Check backend logs for `Permission catalog sync complete: created=…`
2. Manual trigger (OOSM admin UI or API):

```http
GET /api/security/permission/catalog-status
POST /api/security/permission/sync-catalog
Authorization: Bearer <OOSMADMIN token>
```

Or open **Administration → Permission catalog** in the app.

3. Or run the SQL seed (PostgreSQL security DB):

```bash
# From repo root — adjust connection for your DB
psql "$DATABASE_URL" -f "oosm/modules/production/src/main/resources/legacy/osm-prod/db/migration/insert Permissions.sql"
```

4. Force re-sync every startup: `PERMISSIONS_SYNC_ON_STARTUP=true`

Emergency reactivate soft-deleted rows:

```sql
UPDATE permission SET is_deleted = false WHERE is_deleted = true;
```

Then `POST /api/security/permission/sync-catalog`.

## Authority format

JWT authorities: `MODULE:ENTITY:ACTION` (e.g. `INVENTAIR:MATERIEL_SUPPLIER:READ`)

Controllers must use a `getResourceName()` matching the entity key in the catalog (case-insensitive at runtime).
