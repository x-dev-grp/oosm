# Day import remediation plan

Status: prepared for implementation; no application changes or deployment performed.

Basis: [business and technical review](F:/oosm/docs/reviews/day-import-review-2026-09-26.md). Scope: backend `F:\oosm` and frontend `F:\osm-ms-fe`.

## Target behavior

A user reviews a specific workbook, commits it once, and receives a durable result. All business writes belong to the current tenant and are authorized. Retrying a request cannot duplicate payments, expenses, sales, stock movements, or master records. Any invalid row prevents all business writes. Drive uses the same import engine and records file-routing failures separately from import failures.

## Rules to implement

- The complete workbook is the business transaction boundary. No partial-success mode in this change.
- Scope external references by tenant and operation kind. Repeated identical references/payloads are skipped; reuse with different business values is a conflict. Do not silently overwrite.
- Payment identity must be independent of reception identity: multiple legitimate installments require distinct payment references.
- Existing lots keep their current operation type and lifecycle status on retry. Only newly created lots enter finalization through valid domain transitions.
- Validation simulates the import's documented execution order, including receipts before sales, rather than promising arbitrary chronological processing within the day.
- Missing or unknown required references, invalid enum values, orphan sale lines, duplicate conflicting keys, and invalid quantities produce row/field errors. Do not silently substitute CASH, TND, or EXTRA_VIRGIN for an invalid supplied value. Defaults for empty optional fields must be explicit in the template.
- For new-format payment rows, use an explicit paymentDate with businessDate as the documented default; retain actual import time separately. Existing ledger dates are not rewritten. Backdated entries must obey any existing accounting-period restrictions discovered during implementation.
- Templates and UI advertise XLSX only. Retain current sheet names; introduce a template version and payment externalRef/paymentDate fields.
- Preview validity is tied to workbook identity. Commit still revalidates database state; material changes require a fresh preview rather than silently changing the reviewed effects.

## Phase 1 — Establish regression tests and close access gaps

Coverage: review findings 1 and 2. First backend change; independently deployable once verified.

1. Turn the isolated reproductions into maintained production-module tests. Add PostgreSQL integration fixtures for two tenants and restricted/admin users, using a disposable test database. Never inherit development/production datasource configuration.
2. Require a non-null authenticated tenant for every interactive import endpoint. Scheduled imports receive an explicit, trusted tenant execution context.
3. Replace unscoped supplier, tank, reception, invoice, expense, container, and QC lookups with tenant-scoped queries. Include nested references and finalization queries, not just the initial lookup.
4. Validate ownership again at write boundaries. Return generic missing/invalid-reference errors without exposing another tenant's records.
5. Enforce exact module/entity/action permissions on the backend. Require import access plus permissions for the operations actually contained in the workbook, including payment posting and stock approval. Reuse existing authority contracts where correct; add catalog entries only where a capability is missing.
6. Restrict Drive connect/disconnect/configuration to tenant administration. Manual sync requires import authorization. Scheduled sync uses an explicitly enabled tenant automation policy, not a fabricated human admin session. Disabling automation or disconnecting must prevent future scheduled work.
7. Align frontend guards and buttons with the server policy. Ensure denied requests return 401/403 rather than being swallowed into HTTP 200 by controller catch blocks.

Acceptance: two tenants using identical references cannot resolve or change each other's data; foreign UUIDs are rejected; direct API requests from restricted users fail before writes or Google calls.

## Phase 2 — Make commits atomic and retries durable

Coverage: findings 3, 4, and 6. Depends on phase 1.

1. Separate parsing, validation/planning, transactional execution, and result persistence. Both manual and Drive entrypoints call the same orchestration service.
2. Build a complete workbook reference index before execution. Resolve references against both that index and tenant-owned database records. Validate QC rules/values even when the reception is new.
3. Abort execution with a typed exception for every commit-time error. Configure rollback for relevant checked exceptions as well as runtime failures. Do not catch a business error and continue writing rows.
4. Add an import-run record: tenant, source, file digest, template version, business date, actor/automation identity, timestamps, outcome, and immutable report. Preserve failure outcomes outside the rolled-back business transaction without allowing the audit path to commit business data.
5. Add operation identities with a database unique constraint on `(tenant_id, operation_kind, external_ref)`, a canonical business-payload fingerprint, resulting entity ID, and import-run ID. Write identities in the same transaction as business effects. Normalize references consistently without merging legitimately distinct values.
6. Require payment externalRef in the new template. A repeated payment key skips an identical settled operation; changed amount/date/reception is a conflict. Different payment keys on one reception remain valid installments.
7. Protect concurrent claims at the database boundary. A uniqueness conflict rolls back the attempt; resolve the existing completed result in a fresh transaction. Do not swallow an integrity exception in a rollback-only transaction.
8. Track newly created reception IDs separately. Do not finalize duplicate/existing lots. Use domain transition checks, including QC prerequisites, rather than unconditional status assignment.
9. Return explicit committed/replayed/rejected outcomes and structured row errors. Persist and export the actual commit report, not a later dry-run of the same file.

Legacy data and compatibility:

- Existing payment sheets lack reliable payment identity. Do not infer it from row number, file name, amount alone, or file hash.
- Validate legacy workbooks and report the required template upgrade; do not silently execute ambiguous legacy payment rows.
- Before enabling retries against old data, inventory historical description markers and existing payments in a read-only reconciliation report. Backfill operation identities only where the relationship is unambiguous; block ambiguous historical references pending reconciliation. New keys alone cannot prove an old payment has not already been posted.
- Existing imports without payments may be supported where tenant-scoped legacy identities can be resolved safely. Markers are a compatibility aid, not the new identity store.
- Flyway is currently disabled in `application.yml`. Add explicit, repeatable schema SQL through the active deployment/startup mechanism, including the unique indexes; verify actual database constraints. Placing files only under a migration directory will not execute them.

Acceptance: same workbook committed twice has one set of business effects; a late error leaves no business writes; concurrent retries cannot duplicate effects; a modified reused reference is rejected; existing lot state remains unchanged.

## Phase 3 — Validate stock and financial effects as a complete plan

Coverage: finding 5 and validation gaps. Depends on phase 2's planning/execution boundary.

1. Maintain projected per-tank and per-container balances throughout validation. Include new receipts, every sale, and every container line; exclude already-applied operations from projected deltas.
2. Validate tank capacity and existing quality/ownership constraints using domain rules. Validate positive quantities, finite numeric values, prices, installment amounts, currencies, and enum values before writing.
3. Calculate monetary values using the existing domain rounding policy and decimal arithmetic in the import path. Avoid a repository-wide numerical rewrite in this change.
4. Reject payment amounts exceeding the remaining payable balance instead of reporting the requested amount while downstream logic silently clamps it.
5. Lock affected stock/balance records in deterministic order during commit, or use the application's verified optimistic concurrency mechanism. Revalidate against the locked/current state.
6. Check all competing writers, including interactive reception/sale/payment paths. An import-only lock does not protect against an unlocked interactive writer.
7. Distinguish stale-preview conflicts from malformed-workbook errors and return enough detail for a new preview.

Acceptance: 100 L with two 70 L sales fails; a 100 L planned receipt followed by an 80 L sale succeeds subject to domain constraints; cumulative container shortages fail; simultaneous manual/import operations cannot oversell or overpay.

## Phase 4 — Bind the frontend to the reviewed file and actual outcome

Coverage: findings 4 and 8. Depends on the phase 2/3 API contract; backend contract tests can precede UI work.

1. Introduce explicit states: selected → validating → valid/invalid → committing → committed/failed.
2. Attach a request/file identity to every preview. Ignore stale responses, invalidate reports on selection changes, and prevent file changes during commit.
3. Return a server-issued preview identifier tied to tenant, file digest, and planned effects. Commit submits the identifier and exact file; reject mismatches and stale material effects. Do not trust a client-supplied digest alone as proof of a reviewed server preview.
4. Disable commit after a successful run. A network timeout shows an unknown outcome and resolves via run status/safe retry, not an immediate assumption that nothing happened.
5. Show success only for committed/replayed outcomes. Show row errors for rejected imports; separate operational failures from validation errors.
6. Export the saved run report. Surface operation counts, stock/financial totals, skipped duplicates, and conflict reasons before commit.
7. Update templates, guide, translations, and `.xlsx` validation together. Present field errors without exposing stack traces or implementation details.

Acceptance: A's late response cannot enable B's commit; commit errors never produce a success toast; a double click/retry cannot duplicate business effects; the downloaded result matches the executed run.

## Phase 5 — Make Drive synchronization tenant-safe and recoverable

Coverage: finding 7 and Drive operational gaps. Depends on phases 1–3.

1. Persist sync state per tenant/run; remove shared singleton status fields. Use a database-backed tenant lease to prevent overlapping scheduled/manual runs across application instances, with bounded expiry/recovery.
2. Use the same validated, idempotent import engine. Inspect its result explicitly.
3. Record database commit and Drive routing as separate outcomes. If commit succeeds but moving the file fails, retry only the move; display committed/routing-pending status.
4. Page through all Drive results; use bounded request timeouts and retry transient failures without replaying business effects.
5. Make schedule behavior truthful. For this fix, expose the actual server cron and remove the ineffective tenant cron control; per-tenant scheduling is a separate feature.
6. Handle disconnected/revoked credentials and lease recovery explicitly. Keep errors and reports tenant-scoped; never expose credentials in output.

Acceptance: tenants see only their own status; over 100 files are processed; move failure does not repost payments; overlapping runs are coordinated; disconnect disables future work.

## Phase 6 — Integration verification and staged release

1. Run the complete acceptance matrix against a disposable PostgreSQL database and real Spring transaction/security configuration. Unit mocks are insufficient for isolation, rollback, and unique-constraint claims.
2. Run frontend browser tests against the test backend: template download, edited workbook upload, rejected preview, successful commit, saved report, file-switch race, timeout/retry, and restricted user access.
3. Use mocked Drive integration tests for pagination, network errors, moves, and concurrency; perform a controlled OAuth/sync smoke test with a dedicated test folder/account before enabling automation.
4. Rehearse schema changes and legacy reconciliation on a restored nonproduction dataset. Verify indexes, counts, and rollback compatibility.
5. Deploy additive schema and secured backend first, then coordinated frontend/template changes. Keep automated imports disabled until acceptance evidence passes; no production setting is changed by this plan.
6. Pilot a reconciled test tenant, then enable manual imports and finally scheduled Drive sync. Monitor duplicate/conflict counts, rollback outcomes, routing failures, and stock/ledger reconciliation.
7. Rollback means disabling import entrypoints/automation and reverting compatible application changes while preserving audit/identity tables. Never delete identity records or replay old files to undo a release; posted financial corrections use domain reversal/reconciliation procedures.

## Delivery sequence and completion criteria

| Change set | Contents | Dependency |
|---|---|---|
| 1 | Regression fixtures, tenant isolation, server permissions | None |
| 2 | Atomic execution, operation identity, import history, template/payment contract, duplicate finalization | 1 |
| 3 | Aggregate stock/financial validation and concurrency | 2 |
| 4 | Frontend state, preview binding, result/export behavior | 2 and 3 |
| 5 | Tenant-scoped Drive runs and routing recovery | 1–3 |
| 6 | Integration/browser coverage, migration rehearsal, release evidence | All |

Done means all eight review findings have regression coverage, fresh and upgraded databases pass, legacy ambiguity is explicitly accounted for, and the manual plus Drive flows pass end-to-end on isolated infrastructure. A successful compilation alone is not completion.

Out of scope: broad UI redesign, unrelated QR/catalog changes, automatic correction of historical ledger entries, and a repository-wide tenancy or accounting rewrite. Shared defects directly necessary to protect import transactions remain in scope.
