# Day import implementation and release notes

Backend and frontend branch: `codex/day-import-hardening`.

## Implemented

- Tenant-scoped resolution for receptions, sales, suppliers, tanks, containers, QC rules, generic types, and expenses. The generic-type service no longer bypasses the importer's tenant lookup when saving.
- Server authorization for import and workbook operations; tenant administration for Drive connection and manual synchronization. Explicitly enabled scheduled runs use a bounded internal automation context that is cleared after each tenant.
- Durable operation identities for receptions, sales, payments, and expenses, with tenant/kind/reference uniqueness and payload conflict detection. Financial writes and operation identities share the business transaction.
- Database transaction locks serialize imports for a tenant. Version columns on tanks, containers, and receptions reject conflicting writes from other JPA workflows.
- Atomic commits, durable preview/run results, file-bound preview IDs, current-state revalidation, safe replay, and saved report download. Commit errors return failure instead of a success envelope.
- Whole-workbook reference and enum validation; cumulative tank/container balances; payment and sale paid-amount limits; preservation of existing lot state. QC validation runs even for new receptions.
- Template version 2 with independent payment references and payment dates. XLSX-only upload. Invalid numeric cells no longer silently become blank values.
- Tenant-specific Drive status and leases, pagination, bounded HTTP request timeouts, separate committed/routing-pending outcomes, and move-only retries. A changed committed file requires reconciliation instead of being moved silently.
- Frontend preview/file identity checks, commit state management, safe retry after an uncertain response, saved-result downloads, and truthful success/error states.
- Complete English, French, and Arabic import-page dictionaries, including row and Drive statuses. This fixes the raw `ABIOOC.DAY_IMPORT.*` keys shown in the supplied screenshot.

## Compatibility and deployment

Deploy backend and frontend together: the commit endpoint now requires the `previewId` returned by dry-run. Old clients cannot commit without a valid preview ID.

Flyway is disabled in this application. `app/src/main/resources/db/day-import-schema.sql` is registered in the existing Spring SQL startup list. If an environment disables SQL initialization, apply this script explicitly before starting the new backend. The database account needs the required DDL rights. The script is additive and repeatable; it also initializes version columns on existing balance tables.

Use new version-2 templates for payments. A historical marker without a durable identity is blocked for reconciliation. Do not manufacture payment identity from a file name, row number, or amount, and do not replay historical workbooks to test the rollout.

Creation of new generic types requires tenant administration; existing tenant-owned types can be linked. Drive manual sync is administrator-only. The status reports the server cron; the old tenant cron parameter does not configure scheduling.

Keep tenant automatic imports disabled during rollout. Verify a controlled tenant with new data and reconcile historical imports before enabling unattended synchronization. OAuth credentials, production settings, and existing business data were not changed by this implementation.

## Verification

- Full backend reactor: `mvn -B verify -pl app -am` using Java 21.
- Production-module tests: service validation, controller permission failures, versioned workbook generation, Drive recovery/pagination, PostgreSQL transaction rollback/identity uniqueness/leases, and real JPA tenant queries/optimistic stock concurrency.
- PostgreSQL tests use a separately initialized local instance on port 55439 and a dedicated `day_import_review_test` database. Each test context creates its own schema. No application datasource credentials are loaded.
- Frontend TypeScript/Angular compilation and production build.
- Frontend state regression checks and all import translation keys across English/French/Arabic.
- Five Chromium tests against the production bundle: three languages, successful commit, rejected commit. HTTP responses are mocked, and synthetic test tokens are never sent to real APIs.
- CI now provisions a dedicated PostgreSQL service and includes the import regression and browser checks.

The frontend build reports existing bundle-size/CommonJS warnings. Live Google OAuth, a production-data migration rehearsal, and a full browser-to-domain-services test against a restored dataset remain release checks; they are not established by the mocked browser tests. No deployment is included in this push.

## Local checks

Backend PostgreSQL tests are opt-in locally via `DAY_IMPORT_TEST_JDBC_URL`, `DAY_IMPORT_TEST_USER`, and `DAY_IMPORT_TEST_PASSWORD`. The URL must name the dedicated `day_import_review_test` database. CI supplies these values automatically.

Frontend checks:

```text
node --test scripts/day-import-state.test.cjs
node scripts/check-day-import-i18n.mjs
npx playwright test e2e/day-import.spec.ts --config=e2e/playwright.config.ts --project=chromium
```

Rollback preserves the new identity/history tables. Disable imports and revert compatible application versions; never remove identity records to make an old workbook import again.
