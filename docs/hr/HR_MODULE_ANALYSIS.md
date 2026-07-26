# HR Module Analysis — OSM 2.0

**Date:** 2026-07-23  
**Scope:** Full architecture analysis before implementing Tunisian HR & Payroll  
**Principle:** Evolve the existing `modules/hr` module; do not create a parallel architecture.

---

## 1. Current architecture

### 1.1 Platform shape

| Expectation in master prompt | Actual OSM 2.0 |
|------------------------------|----------------|
| Microservices + Eureka + Gateway | **Modular monolith** (`oosm`) — single JAR on port **8084** |
| `hrservice`, `financeservice`, … | Maven modules under `oosm/modules/*` |
| `/api/hr/**` via gateway | Controllers served directly; tenant module gate via `TenantModuleApiAccessFilter` |

**Stack:** Spring Boot 3.4.4, Java 21, PostgreSQL, Angular 19 + Able Pro.

| Path | Role |
|------|------|
| `oosm/` | Backend modular monolith |
| `osm-ms-fe/` | Angular SPA |
| `oosm/modules/hr` | Active HR domain |
| `oosm/modules/shared-kernel` | BaseEntity, BaseServiceImpl, ports |
| `oosm/modules/security` | JWT, roles, permissions, notifications |
| `oosm/modules/finance` | Ledger, expenses (integration via ports) |

### 1.2 Base entity conventions

`com.xdev.ooms.sharedkernel.entities.BaseEntity`:

| Field | Notes |
|-------|-------|
| `UUID id` | `@GeneratedValue(UUID)` |
| `UUID tenantId` | Auto-stamped from `TenantContext` |
| `Boolean isDeleted` | Soft delete |
| `createdBy` / `lastModifiedBy` | Spring Data auditing |
| `createdDate` / `lastModifiedDate` | `@PrePersist` / `@PreUpdate` |
| `qrHex` / `qrImageBase64` | Optional QR |

Do **not** duplicate these on HR entities. Field naming uses `lastModifiedBy` / `lastModifiedDate` (not `updatedBy` / `updatedAt`).

### 1.3 CRUD / DTO / service pattern

```
feature/
  entity/ dto/ repository/ service/ controller/
```

- Controllers extend `BaseControllerImpl`
- Services extend `BaseServiceImpl`
- Nested DTOs: `{ employee: { id } }` — never flat `employeeId` in API
- Relations resolved in `resolveEntityRelations()` via `HrRelationResolver`
- **No** HR-specific `ModelMapper` configuration
- Business rules in `HrBusinessLinkageService`

### 1.4 Security

- Authority format: `MODULE:ENTITY:ACTION` (e.g. `HR:EMPLOYEE:READ`)
- Catalog: `permissions/permissions-spec.json`
- HR roles: `HR_CLERK`, `HR_MANAGER`, `HR_PAYROLL_OFFICER`, `HR_ADMIN`
- Tenant module gating: `/api/hr/` → `OOSMModule.HR`
- Admin roles `ADMIN` / `OOSMADMIN` bypass via `HrPermissionSupport`

### 1.5 Database

| Mechanism | Status |
|-----------|--------|
| Flyway | Dependency present; **`spring.flyway.enabled: false`** |
| Hibernate | `ddl-auto: update` (default) |
| Manual SQL | `app/src/main/resources/db/*.sql` |
| HR migrations | **None** — tables via Hibernate entity mapping |

### 1.6 Cross-module integration

| Port | Used by HR? |
|------|-------------|
| `ExpensePort` | **No** |
| `FinancialTransactionPort` | **No** |
| `NotificationPort` | **No** |
| `HrPayRollReadPort` | **Yes** (payslip PDF) |

Events are in-process ports (no Kafka/RabbitMQ).

---

## 2. Existing HR code (baseline)

### 2.1 Backend packages (`com.xdev.ooms.hr`)

| Package | Table | Capability |
|---------|-------|------------|
| `employee` | `hr_employee` | Identity, CIN, CNSS, contact, hire, free-text dept/job |
| `poste` | `hr_poste` | Position title/description |
| `contract` | `hr_employment_contract` | Employee↔poste, type, dates, salary (`Double`) |
| `pointage` | `hr_pointage` | Daily attendance summary |
| `leave` | `hr_leave_request` | Request + approve/reject |
| `payroll` | `hr_payroll_period` | Monthly period OPEN→CLOSED |
| `payslip` | `hr_payslip` | Fixed CNSS/IRPP/CSS columns + PDF |
| `common` | — | Enums, linkage, calculator, permissions |

### 2.2 Shared helpers

- `HrRelationResolver` — nested FK load
- `HrBusinessLinkageService` — CIN/CNSS uniqueness, active contracts, leave overlap, payslip enrichment, locked periods
- `HrPayrollCalculator` — **hardcoded** CNSS 9.68%/17.07%, IRPP brackets, SMIG, uses `double`
- `PayslipPdfService` — iText PDF
- `PayrollPeriodService.generatePayslips` — creates payslips from active contracts

### 2.3 Frontend (`osm-ms-fe/src/app/hr`)

- Lazy `/hr` module with `moduleGuard(HR)`
- `HrDashboardComponent` — navigation cards (no KPI charts yet)
- `HrEntityListComponent` + `hr-list-registry.ts` + `oosm-dashboard`
- Forms for all 7 entities; nested DTO helpers in `hr-form.utils.ts`
- **Duplicated** Tunisian rates in `tunisia-hr.constants.ts` and `hr-payroll.utils.ts`
- i18n: `en` / `fr` / `ar` under `HR.*`

### 2.4 Existing enums (incomplete vs target)

| Enum | Current values |
|------|----------------|
| `EmployeeStatus` | ACTIVE, SUSPENDED, TERMINATED |
| `ContractType` | CDI, CDD, INTERNSHIP, TEMPORARY |
| `ContractStatus` | (present) |
| `LeaveType` / `LeaveStatus` | request-level only |
| `PayrollPeriodStatus` | OPEN, CALCULATED, VALIDATED, PAID, CLOSED |
| `WorkRegime` | HOURS_48, HOURS_40, AGRICULTURAL |

---

## 3. Reusable components

| Layer | Reuse |
|-------|-------|
| Backend CRUD | `BaseControllerImpl` / `BaseServiceImpl` / `BaseRepository` |
| Relations | `HrRelationResolver` pattern |
| Permissions | Extend `permissions-spec.json` + `Action` enum |
| Finance posting | `ExpensePort` / `FinancialTransactionPort` |
| Notifications | `NotificationPort` + `notification-rules-spec.json` |
| PDF | iText pattern from `PayslipPdfService` / `document-generation` |
| Frontend lists | `HrEntityListComponent` + dashboard configs |
| Nested fields | `hr-nested-dashboard.fields.ts` |
| Forms | `toEmployeeRef` / `toPosteRef`, confirmation dialog, toast |
| Auth | `allPermissionGuard`, `auth.hasPermission` |

---

## 4. Architectural gaps (vs master requirements)

### 4.1 Critical payroll / legal

| Gap | Risk |
|-----|------|
| Hardcoded CNSS/tax/SMIG in Java **and** Angular | Legal change requires code deploy; FE/BE can diverge |
| Monetary amounts as `Double` | Rounding / precision errors |
| No calculation snapshot / line items | Historical payroll not reproducible |
| No salary component model | Cannot model overtime, allowances, deductions cleanly |
| No effective-dated legal rules | Cannot version Tunisian law |
| No company HR legal profile | Wrong CNSS regime assumptions for olive mill |

### 4.2 Domain coverage missing

- Department / Grade / Category (as entities)
- Contract amendments, CDD legal reasons, probation rules
- Work schedules / shifts (olive campaign)
- Raw attendance events, timesheets, anomaly workflow
- Overtime rules + approval
- Leave types/policies/balances/transactions
- Public holidays
- Advances / loans
- Employee documents
- Compliance engine
- HR Agent (AI tools over deterministic backend)
- Finance PAYROLL_VALIDATED integration
- CNSS / tax declaration reports
- KPI dashboard / analytics

### 4.3 Cross-cutting

| Gap | Notes |
|-----|-------|
| HR notifications | No rules in catalog |
| Versioned DB migrations | Hibernate update only |
| Automated HR tests | None found |
| File storage for documents | `modules/storage` is placeholder |
| Schedulers | No `@EnableScheduling` for HR |
| Optimistic locking | No `@Version` on HR entities |

### 4.4 Security / tenancy concerns (baseline defects)

Documented in Frappe blueprint / production readiness audit:

1. Generic CRUD may not always enforce tenant scope on every path
2. Action decoration ≠ full server-side authorization for all operations
3. Soft-delete only — correct for employees with history; ensure hard delete blocked

---

## 5. Risks

| Risk | Mitigation |
|------|------------|
| Breaking existing HR data during schema evolution | Additive columns; map free-text `department`/`jobTitle` carefully; soft deprecation |
| Hibernate `ddl-auto=update` alone | Provide SQL patch scripts + document migration; prefer additive changes |
| Incorrect Tunisian legal parameters | Seed as **configurable** provisional values; flag for accountant validation |
| Scope explosion (Frappe parity) | Phased delivery per master prompt §51; Agent last |
| FE preview diverging from BE | Remove FE rate constants; FE displays BE results only |
| Validated payroll mutation | Lock periods; adjustments/reversals only |

---

## 6. Migration concerns

### 6.1 Existing tables to preserve

`hr_employee`, `hr_poste`, `hr_employment_contract`, `hr_pointage`, `hr_leave_request`, `hr_payroll_period`, `hr_payslip`

### 6.2 Strategy

1. **Additive** columns on existing entities (employeeNumber, gender, nested department FK, etc.)
2. New tables with `hr_` prefix for legal rules, schedules, advances, etc.
3. Keep `Double` columns temporarily where present; introduce `BigDecimal` on new fields and migrate calculators to `BigDecimal` with conversion at edges
4. Seed default legal rules (CNSS, tax brackets, SMIG) as data — not Java constants
5. Deprecate `HrPayrollCalculator` static doubles; route through legal-rule resolver
6. Manual SQL patch under `app/src/main/resources/db/` for environments that disable ddl-auto
7. **No destructive** recreate; no `ddl-auto=create`

### 6.3 Compatibility

| Old | New | Mapping |
|-----|-----|---------|
| `Employee.department` (String) | `Department` entity | Keep string until FK populated; dual-read |
| `Employee.jobTitle` (String) | Prefer `Poste` / contract | Keep for display fallback |
| `Payslip` fixed tax columns | `PayrollLine` + snapshot | Keep columns; fill from engine; add lines table |
| `HrPayrollCalculator` rates | `LegalRule` / `SocialSecurityConfiguration` | Engine reads config; calculator becomes thin adapter |

---

## 7. Recommended implementation architecture

### 7.1 Keep modular monolith

```text
oosm/modules/hr
  employee / organization / contract / schedule
  attendance / timesheet / overtime / leave / holiday
  payroll/engine / component / variable / configuration
  payslip / advance / loan / legal / document
  compliance / reporting / integration / agent / audit
```

APIs remain under `/api/hr/**`.

### 7.2 Legal configuration foundation (Phase 1 priority)

```text
LegalRule (effective-dated, category, configurationJson)
CompanyHrLegalProfile
SocialSecurityConfiguration
TaxConfiguration + TaxBracket
MinimumWageRule
SalaryComponent
```

Payroll engine:

```text
PayrollEngine → Gross / Overtime / Absence / CNSS / Tax / Net / EmployerCost
```

All money: **`BigDecimal`** with explicit rounding.  
All rates: from effective-dated config for the payroll period.  
On calculate/validate: persist **configuration snapshot**.

### 7.3 Finance integration

On `PAYROLL_VALIDATED` / `PAY`:

```text
HR PayrollPeriodService
  → PayrollValidatedCommand (idempotent key = payrollRunId)
  → ExpensePort / FinancialTransactionPort (salary, CNSS, tax payables)
```

### 7.4 HR Agent (Phase 5 only)

```text
LLM orchestrates → tool registry → existing services → structured result → explanation
```

Never: LLM invents payroll amounts.

### 7.5 Frontend

Extend existing HR module:

- Registry + dashboard configs for new entities
- Employee detail tabs (overview, contract, payroll, …)
- Settings area for legal/CNSS/tax/SMIG/components
- KPI dashboard (ApexCharts like finance hub)
- Remove client-side statutory calculation as source of truth

### 7.6 Implementation order (aligned with master prompt)

1. Analysis ← **this document**
2. Legal config + company profile + org + employee + contract + documents
3. Schedules, attendance, timesheets, overtime, leave, holidays
4. Salary structures, payroll engine, payslips
5. Advances, loans, finance, reports
6. Compliance, notifications, dashboard, HR Agent

---

## 8. Decision log

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Microservice vs monolith | **Stay monolith** | Matches OSM; ports already cross-module |
| Discard vs evolve HR | **Evolve** | Existing data, permissions, UI patterns |
| Monetary type | **BigDecimal in engine**; migrate entities gradually | Avoid precision bugs without big-bang schema break |
| Legal rates | **DB-configured, effective-dated** | Tunisian law changes without code rewrite |
| Migrations | Hibernate update + SQL patches | Flyway disabled in current deploy model |
| Agent timing | After payroll domain stable | Spec §51 Phase 5 |

---

## 9. Next step

Proceed to **Phase 1 implementation**:

1. Legal rule entities + seed provisional Tunisian rates  
2. Company HR legal profile  
3. Department / Grade / Category  
4. Employee enrichment + soft lifecycle  
5. Contract legal validation (CDD reason, probation, min wage)  
6. Document metadata foundation  

Then Phase 2–5 per master prompt, updating living docs to match **actual code**.
