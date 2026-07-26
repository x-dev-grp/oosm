# HR Implementation Summary — OSM 2.0

**Date:** 2026-07-23  
**Approach:** Evolve existing `modules/hr` inside the modular monolith (not a new microservice).

---

## Architecture implemented

- Modular monolith HR domain under `com.xdev.ooms.hr`
- Effective-dated **legal configuration** (CNSS, tax, SMIG, components, company profile)
- Deterministic **PayrollEngine** (BigDecimal) — rates from DB only
- Attendance → timesheet validation gate; overtime; leave configs; holidays
- Advances / loans; compliance scan; dashboard KPIs; finance port on payroll validate
- HR Agent as **tool router** over services (no LLM-invented payroll)
- Angular Able Pro HR module extended (lists, settings, compliance, agent, KPI dashboard)

See: `HR_MODULE_ANALYSIS.md`, `HR_ARCHITECTURE.md`.

---

## Entities / packages added or extended

**Extended:** Employee, EmploymentContract, Pointage, Payslip, PayrollPeriod, enums  

**New domains:** organization, document, schedule, timesheet, overtime, leave configs, legal, payroll.engine, payroll variables, advance, loan, compliance, dashboard, agent, integration.finance, notification  

**Scale:** ~237 backend Java files; ~167 frontend HR files.

---

## Database

- Hibernate additive schema (`hr_*` tables)
- Flyway remains disabled (project standard)
- Migration guidance: `HR_MIGRATION_PLAN.md`, `HR_DATABASE_MODEL.md`

---

## APIs

Full CRUD under `/api/hr/**` plus special endpoints for seed, leave/OT approve, timesheet validate, dashboard, compliance scan, agent query, payslip PDF.  
Details: `HR_API.md`.

---

## Angular pages

| Route area | Status |
|------------|--------|
| `/hr` dashboard KPIs | Done |
| Employees / postes / contracts / pointages / leave / payroll / payslips | Extended |
| Departments, grades, schedules, timesheets, OT, leave types, holidays | Done |
| Advances, loans, documents, payroll variables | Done |
| Settings (legal, CNSS, tax, SMIG, components, company profile) | Done |
| `/hr/compliance`, `/hr/agent` | Done |

---

## Payroll engine

Documented in `HR_PAYROLL_ENGINE.md`.  
Payslips store `calculationSnapshot` + `calculationBreakdown`.  
`HrPayrollCalculator` is a facade — **no hardcoded rates**.

---

## CNSS / tax / legal

Seed via `POST /api/hr/legal-rules/seed-defaults` (provisional).  
Must be validated by Tunisian payroll specialist — see `HR_TUNISIAN_LEGAL_CONFIGURATION.md`.

---

## Security / permissions

Extended `permissions-spec.json` with legal, org, time, compliance, agent entities and role grants.  
Backend authorization via `HrPermissionSupport` for sensitive actions.  
See `HR_SECURITY.md`.

---

## Finance integration

```text
PayrollPeriod → VALIDATED
  → PayrollFinanceIntegrationService (idempotent financePosted)
  → PayrollAccountingPort
  → PayrollAccountingPortImpl → ExpensePort
```

---

## HR Agent tools

Keyword router + tool registry + `HrAgentActionLog`.  
See `HR_AGENT.md`.

---

## Tests implemented

| Suite | Result |
|-------|--------|
| SocialSecurity / IncomeTax / PayrollEngine / SalaryCompliance | Pass |
| ContractLegalValidator / AttendanceAnomalyDetector | Pass |
| Monolith `app` compile | Pass |
| Angular HR build (during FE work) | Pass |

---

## Remaining risks

1. Provisional legal rates not lawyer/accountant-certified  
2. Mid-month prorating / full payroll reversal entities incomplete  
3. Payslip persistence still uses Double at DB edge (engine is BigDecimal)  
4. Self-only payslip scoping needs tighter IDOR tests  
5. Document binary storage still metadata/ref-based (no dedicated blob store)  
6. Agent is deterministic keyword router — not yet LLM-backed  
7. Hibernate ddl-auto vs formal Flyway migrations  

---

## Legal parameters requiring specialist validation

- CNSS employee/employer rates and base rules  
- CSS / accident contribution  
- IRPP brackets and family deductions  
- SMIG / SMAG by regime and sector  
- CDD probation maxima  
- Overtime multipliers and campaign special rules  
- Annual leave allowances by category / seniority  

---

## Known technical debt

- Full `PayrollAdjustment` / `PayrollReversal` workflow  
- Leave accrual scheduler  
- Richer CNSS quarterly export formats  
- Optimistic `@Version` on sensitive entities  
- Exhaustive security IDOR suite  
- Replace keyword agent with LLM orchestration (same tools)

---

## Recommended next steps

1. Accountant review of seeded rates per tenant profile  
2. Configure `CompanyHrLegalProfile` for industrial vs agricultural activities  
3. Automate payroll scenarios 1–13 as integration tests  
4. Enable Flyway for HR schema versioning when ops ready  
5. Plug LLM into agent with same tool registry  
6. Harden payslip self-access and cross-tenant tests  

---

## Implementation checklist

```text
[x] Backend compiles (modules/hr + app)
[x] Frontend compiles (HR module build during implementation)
[x] Database migrations valid (additive Hibernate; documented plan)
[x] Tenant isolation pattern reused (TenantContext + filters + module gate)
[x] Payroll unit tests pass
[ ] Security tests pass (manual suite remaining)
[x] Payslip PDF works (existing PayslipPdfService retained)
[x] Finance integration implemented (port + idempotent post on validate)
[x] Agent permissions modeled (HRAGENT + confirmation for writes)
[x] Legal rules configurable (DB + seed)
[x] No critical payroll constants hardcoded in engine
[x] Historical payroll reproducible (calculationSnapshot on payslip)
```

---

## Authority principle (delivered)

```text
Tunisian law / company rules
          ↓
Versioned legal configuration
          ↓
Deterministic HR & Payroll Engine
          ↓
Validated and auditable HR data
          ↓
Compliance / Reporting / Finance
          ↓
HR Agent (orchestrates, does not invent amounts)
```
