# OSM Permission Catalog

Single source of truth for which `(module, entity, action)` permissions exist in OSM.

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
- Merges legacy aliases and role mirrors

## Authority format

JWT authorities: `MODULE:ENTITY:ACTION` (e.g. `INVENTAIR:MATERIEL_SUPPLIER:READ`)

Controllers must use a `getResourceName()` matching the entity key in the catalog (case-insensitive at runtime).
