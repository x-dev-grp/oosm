# HR Architecture — OSM 2.0

**Status:** Implemented (evolved modular monolith)  
**Backend:** `oosm/modules/hr` (~237 Java files)  
**Frontend:** `osm-ms-fe/src/app/hr` (~167 files)

---

## Platform decision

HR remains inside the **OOSM modular monolith**. There is no separate `hrservice` / Eureka / gateway. APIs are served at `/api/hr/**` on the monolith (port 8084), gated by `TenantModuleApiAccessFilter` → `OOSMModule.HR`.

```text
security (JWT, roles, permissions)
        ↓
OosmMonolithApplication
  ├── modules/hr          ← domain authority
  ├── modules/finance     ← PayrollAccountingPortImpl
  ├── modules/shared-kernel (BaseEntity, ports)
  └── …
        ↓
Angular /hr (lazy, Able Pro)
```

---

## Package map

| Package | Responsibility |
|---------|----------------|
| `employee` | Personnel master + detail aggregates |
| `organization` | Department, Grade, EmployeeCategory |
| `poste` | Positions |
| `contract` | Employment contracts, amendments, `ContractLegalValidator` |
| `document` | Employee documents metadata |
| `schedule` | Work schedules (admin / shifts / campaign) |
| `pointage` | Attendance + anomaly detection |
| `timesheet` | Validated timesheets (payroll input gate) |
| `overtime` | Overtime rules + requests |
| `leave` | Leave requests, type configs, balances, public holidays |
| `legal` | Legal rules, CNSS, tax, SMIG, salary components, company profile |
| `payroll` | Periods + **engine** (BigDecimal, config-driven) |
| `payslip` | Payslips + PDF |
| `advance` / `loan` | Salary advances, loans, installments |
| `compliance` | Scan engine + violations |
| `dashboard` | KPI stats API |
| `agent` | Deterministic tool router + action log |
| `integration.finance` | PAYROLL_VALIDATED → finance port |
| `notification` | Leave-approved notifications |

---

## Cross-cutting patterns

- **Entities:** extend `BaseEntity` (UUID, tenantId, soft-delete, audit)
- **Money (engine):** `BigDecimal` via `HrMoney`
- **Legacy payslip columns:** still `Double` at persistence edge; engine converts
- **Relations:** `@ManyToOne` + nested DTOs; `HrRelationResolver`
- **Business rules:** `HrBusinessLinkageService` + domain validators
- **Permissions:** `HR:ENTITY:ACTION` from `permissions-spec.json`
- **Legal rates:** never hardcoded in calculators — `LegalConfigurationService`

---

## Payroll authority chain

```text
Tunisian / company rules (DB, effective-dated)
        ↓
LegalConfigurationService
        ↓
PayrollEngine (deterministic Java)
        ↓
Payslip + calculationSnapshot / breakdown
        ↓
Compliance / Finance / Agent explanation
```

The HR Agent **never** invents payroll amounts; it calls tools that call services/engine.

---

## Key APIs

| Area | Base path |
|------|-----------|
| Employees | `/api/hr/employees` |
| Org | `/api/hr/departments`, `/grades`, `/employee-categories`, `/postes` |
| Contracts | `/api/hr/contracts`, `/contract-amendments` |
| Time | `/api/hr/work-schedules`, `/pointages`, `/timesheets`, `/overtime-*` |
| Leave | `/api/hr/leave-requests`, `/leave-types`, `/leave-balances`, `/public-holidays` |
| Legal | `/api/hr/legal-rules`, `/social-security-configs`, `/tax-*`, `/minimum-wage-rules`, `/salary-components`, `/company-legal-profile` |
| Payroll | `/api/hr/payroll-periods`, `/payroll-variables`, `/payslips` |
| Advances/Loans | `/api/hr/salary-advances`, `/employee-loans` |
| Ops | `/api/hr/dashboard/stats`, `/compliance`, `/agent/query` |
| Seed | `POST /api/hr/legal-rules/seed-defaults` |

---

## Frontend structure

- Registry-driven lists: `HrEntityListComponent` + `hr-list-registry.ts`
- Nested dashboard fields: `hr-nested-dashboard.fields.ts`
- Settings / Compliance / Agent special pages
- KPI dashboard via `/api/hr/dashboard/stats`
- FE statutory constants are **provisional display fallback only** — backend is source of truth
