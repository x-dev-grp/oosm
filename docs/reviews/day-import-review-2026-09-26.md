# Day import: business and technical review

Reviewed 2026-09-26. Backend: `F:\oosm`, commit `a319606`. Frontend: `F:\osm-ms-fe`, commit `fd3d727`.

## Assessment

The feature is implemented and compiles, but the import is not ready for unattended financial processing. Tenant isolation, authorization, payment retries, and commit-result handling have blocking defects. The earlier saved 39-row success reports demonstrate one historical happy path; they do not establish retry safety or production readiness.

This review changed no application source or business data. Findings below distinguish executed isolated checks from source inspection. No live API commit, Drive sync, or account connection was performed.

## Business flow and feature coverage

| Stage | Implemented behavior | Review result |
|---|---|---|
| Access | Reception import page guarded by reception-create permission | Backend does not enforce equivalent permission; import also writes finance and sales |
| Preparation | Blank/sample XLSX with guide, master data, receptions, QC, payments, sales, container lines, expenses | StorageUnits is a dropdown reference sheet; tanks must already exist |
| Validation | Parse workbook; resolve existing records; preview CREATE/LINK_EXISTING/SKIP_DUPLICATE/ERROR and totals | Incomplete reference checks and aggregate stock simulation |
| Commit | Re-parse and revalidate uploaded workbook inside a transaction, then process dependencies in order | Commit-time report errors do not necessarily throw or roll back |
| Reception | Create supplier-linked lots, optional stock-in, QC, payments, final status | Unknown suppliers can be silently omitted; duplicate lots are still finalized |
| Sales | Create sale/container lines and approve stock movement | Stock validation compares individual rows with current stock |
| Finance | Reception settlements and expenses through existing services | Payments lack import idempotency; invalid enum values can default silently |
| Feedback | Onscreen report; XLSX/CSV report download | Commit error reports can display success; downloaded reports rerun dry-run rather than preserve commit evidence |
| Drive | Tenant OAuth credentials, manual/scheduled sync, processed/failed folders | Shared status across tenants; database commit and file movement can disagree |

Processing order: regions/parcels/supplier types → QC rules → suppliers → containers → receptions → QC results → payments → oil sales → expenses → reception status finalization.

## Findings

### 1. P1 — Import lookups do not enforce tenant ownership

[Payment lookup](F:/oosm/modules/production/src/main/java/com/xdev/ooms/production/dayimport/service/DayImportService.java:875), [supplier lookup](F:/oosm/modules/production/src/main/java/com/xdev/ooms/production/dayimport/service/DayImportService.java:1171), [tank lookup](F:/oosm/modules/production/src/main/java/com/xdev/ooms/production/dayimport/service/DayImportService.java:1189).

Reception stamps, invoices, suppliers, and tanks are resolved using repository methods without tenant predicates. Supplier fallback even scans `findAll()`. `TenantContext` identifies the caller but does not automatically scope these queries: BaseEntity contains an ordinary tenantId field, and no Hibernate tenant filter or database RLS setup was found in the reviewed repository.

Two companies using the same external reference can match each other's records. Payment processing can receive another company's reception ID; tank/supplier resolution can link foreign records. The isolated check set different current and reception tenants and confirmed the importer still invoked payment processing for the foreign reception. This is a mocked service reproduction, not a live cross-tenant database test.

Required correction: tenant predicates on every resolution/deduplication query, ownership checks at service boundaries, and two-tenant regression tests.

### 2. P1 — Backend authorization is authentication-only

[Import controller](F:/oosm/modules/production/src/main/java/com/xdev/ooms/production/dayimport/controller/DayImportController.java:102), [API security](F:/oosm/modules/security/src/main/java/com/xdev/ooms/security/securityConfig/AuthServerConfig.java:174), [frontend guard](F:/osm-ms-fe/src/app/reception/reception.routes.ts:252).

The frontend requires reception CREATE permission, but the controller/service has no corresponding permission enforcement. The API filter requires authentication only. No applicable authorization aspect/interceptor was found. A signed-in user can bypass the route guard and directly request imports, Drive sync, connection, or disconnection. The importer can create expenses, post payments, and approve stock movements in addition to receptions.

Required correction: explicit server-side import authorization, defined permissions for its financial/stock effects, and separate Drive administration permission. Verify forbidden calls with a low-privilege account.

### 3. P1 — Retrying a workbook posts payments again

[Payment processing](F:/oosm/modules/production/src/main/java/com/xdev/ooms/production/dayimport/service/DayImportService.java:866), [settlement behavior](F:/oosm/modules/production/src/main/java/com/xdev/ooms/production/unifieddelivery/service/UnifiedDeliveryService.java:1271).

Receptions are deduplicated, but payment rows have no unique external reference or imported-payment lookup. Every commit invokes `processPayment` again. A 30 TND installment on a 100 TND reception becomes 60 TND paid after importing the same workbook twice. Once fully settled, another retry throws because no payable balance remains. The downstream service caps overpayment, but cannot distinguish a retry from a legitimate installment.

Executed check: the same payment row invoked the payment service on both commits. Monetary consequences follow from inspection of `updateDelivery` and financial transaction creation, not a live ledger mutation.

Required correction: a tenant-scoped payment import key, durable uniqueness, and retry/concurrency tests. Reception-level deduplication cannot substitute for payment-level identity.

### 4. P1 — Partial commit can be returned and displayed as success

[Dry-run reference handling](F:/oosm/modules/production/src/main/java/com/xdev/ooms/production/dayimport/service/DayImportService.java:880), [commit result](F:/oosm/modules/production/src/main/java/com/xdev/ooms/production/dayimport/service/DayImportService.java:235), [success response](F:/oosm/modules/production/src/main/java/com/xdev/ooms/production/dayimport/controller/DayImportController.java:111), [success toast](F:/osm-ms-fe/src/app/reception/import/reception-import-wizard.component.ts:124).

A payment referencing a nonexistent reception passes dry-run because the code assumes it may be created in the workbook without verifying that such a row exists. During commit it becomes an ERROR row, but processing continues. `commit` returns the report without rejecting commit-time errors; the controller returns `success=true`, and the frontend displays the success toast. Drive sync ignores the returned commit report as well.

Executed check: a new region plus a nonexistent payment reference passed dry-run. Commit analysis invoked the region save, returned one payment error, and threw no exception. This demonstrates the service control flow; actual transaction persistence was not exercised. A separate frontend check confirmed an error-bearing commit report still displays COMMIT_OK.

Required correction: validate references against both existing records and the workbook; fail the transaction on any commit-time error; make API, UI, and Drive status reflect the actual outcome.

### 5. P2 — Stock preview does not simulate the whole day's movements

[Tank validation](F:/oosm/modules/production/src/main/java/com/xdev/ooms/production/dayimport/service/DayImportService.java:958).

Each sale compares its quantity with the tank's current volume. Dry-run does not maintain a running balance including earlier sales or planned incoming receptions. Container checks similarly do not reserve stock cumulatively across lines/sales.

Executed check: a tank with 100 L accepted two 70 L sales, producing `canCommit=true` and 140 L stock-out. Conversely, a valid receipt-then-sale file can be rejected if the opening balance is too low even though the receipt supplies enough oil. This finding establishes incorrect preview validation; it does not claim that downstream stock services permit negative inventory.

Required correction: simulate opening balance + planned receipts − planned sales per tank/container, and recheck with concurrency protection at commit.

### 6. P2 — Duplicate receptions still have business state rewritten

[Finalization](F:/oosm/modules/production/src/main/java/com/xdev/ooms/production/dayimport/service/DayImportService.java:788).

Duplicate receptions enter `receptionCache` as SKIP_DUPLICATE, but finalization iterates every workbook reception and rewrites operation type/status to its import-derived target. A lot whose production lifecycle advanced after its first import can be moved back to PROD_READY/IN_STOCK or otherwise reclassified when the old workbook is retried. This contradicts skip/no-overwrite behavior. Source-confirmed; no live lot was modified.

Required correction: finalize only receptions newly created in the current import, using valid domain transitions.

### 7. P2 — Drive operational status is shared across tenants

[Shared fields](F:/oosm/modules/production/src/main/java/com/xdev/ooms/production/dayimport/service/DayImportDriveService.java:35), [status response](F:/oosm/modules/production/src/main/java/com/xdev/ooms/production/dayimport/service/DayImportDriveService.java:75).

The singleton stores last result, last error, timestamps, and counters in one set of atomics. Every tenant receives those same values. Another tenant's file name/error may appear, and counts can combine unrelated companies. Atomics protect individual updates, not tenant isolation or coherent multi-field snapshots. Source-confirmed.

Required correction: tenant-keyed durable sync runs/status with coordinated updates and a per-tenant execution lock.

### 8. P2 — File selection can invalidate the reviewed preview

[File selection and dry-run callback](F:/osm-ms-fe/src/app/reception/import/reception-import-wizard.component.ts:58).

Start validation for A, select B before A completes, then receive A's response. The callback installs A's report while `selectedFile` is B, enabling B's commit without the user reviewing B's rows/totals. The server revalidates B, so this is a preview/consent mismatch rather than a bypass of server validation. After a successful commit, the commit button also remains enabled when the returned report has `canCommit=true`.

Executed check: actual component method bodies, transpiled in an isolated harness with mocked dependencies, submitted B using A's report.

Required correction: bind each report to file/request identity, discard stale responses, coordinate busy states, and consume the successful commit state.

## Additional business and operational gaps

- Payment transaction dates use `LocalDateTime.now()` downstream, while workbook receptions/sales use the business date. Historical imports can put settlement activity in a different accounting period. Whether settlement should use business date or actual payment date needs an explicit product rule.
- Invalid payment methods/currencies/quality grades can silently fall back to CASH/TND/EXTRA_VIRGIN. Invalid quality data must not silently become a higher quality designation.
- A successful database commit followed by a failed Drive move is counted as a failed file and may be moved to the failed folder. Retrying can then hit the payment defect. Persist import outcome separately from file-routing outcome.
- Drive lists one page of up to 100 files without requesting/following `nextPageToken`.
- The displayed tenant `IMPORT_GDRIVE_CRON` is not used by the scheduled annotation, which reads the server property `oosm.import.gdrive.cron`.
- The file picker accepts `.xls`, but the reader uses `XSSFWorkbook`, which handles the XLSX format.
- No durable import-run history, immutable commit report, or atomic import-key uniqueness was found in the reviewed feature. Description-marker deduplication is vulnerable to concurrent imports and mutable descriptions.

## Verification performed

| Check | Result | Limit |
|---|---|---|
| Java 21 Maven production reactor (`-pl modules/production -am test`) | BUILD SUCCESS | No production-module test sources; not behavioral coverage |
| Frontend `tsc --noEmit -p tsconfig.app.json` | Passed | Static TypeScript checks |
| Angular `ngc -p tsconfig.app.json --noEmit` | Passed | Angular compilation; no browser interaction |
| Java isolated service harness | Reproduced missing-reference acceptance, save-before-error return, repeated/foreign-tenant payment invocation, aggregate stock defect | Mockito dependencies; no Spring transaction manager or real DB |
| Frontend isolated method harness | Reproduced stale-file report and false success toast | Mocked HTTP/UI dependencies; no rendered browser |
| Local runtime availability | Frontend listening on 4200; no listener on backend port 8084 | No complete local end-to-end session |

Harnesses and output were generated under `C:\Users\Smail\AppData\Local\Temp\oosm-day-import-review`. Maven output: `C:\Users\Smail\AppData\Local\Temp\oosm-review-maven.log`.

## Release gates

1. Enforce tenant ownership and backend permissions.
2. Make retries idempotent and any commit-time error atomic.
3. Correct aggregate stock validation and preserve existing lot state.
4. Bind frontend previews to files and make result reporting truthful.
5. Isolate Drive runs and distinguish committed data from file-routing failures.
6. Run integration tests for two tenants, restricted users, repeated/concurrent files, rollback, stock totals, and partial/full payments; then verify the UI and Drive flow against an isolated test database.
