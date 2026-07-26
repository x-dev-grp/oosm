# HR & Payroll — Status & Next Steps (Backend)

**Repo:** `oosm`  
**Branch:** `feature/hr-payroll-module`  
**Paired frontend:** `osm-ms-fe` → same branch name  
**Last updated:** 2026-07-26

---

## 1. What we were working on

A production-oriented **Tunisian HR / Payroll** expansion of the existing OSM modular monolith HR module (`modules/hr`), without introducing a separate `hrservice` microservice.

### Goals

- Employee, contract, attendance, leave, payroll, payslips end-to-end  
- **Configurable** CNSS / IRPP / SMIG / overtime / leave rules (no hardcoded law in calculators)  
- Deterministic **Java payroll engine** (`BigDecimal`) as source of truth  
- Finance posting when payroll is validated  
- Compliance scanning + dashboard KPIs  
- HR Agent as a **tool router** (LLM may plan/explain later; never invent payroll amounts)  
- Full Angular UI in the paired frontend repo  

### Architectural decision

| Prompt assumption | Actual OSM |
|-------------------|------------|
| Microservices + gateway | **Modular monolith** (`oosm` JAR, port 8084) |
| Discard / rewrite HR | **Evolve** existing `modules/hr` + permissions + UI patterns |

Analysis baseline: `docs/hr/HR_MODULE_ANALYSIS.md`.

---

## 2. What was delivered (backend)

### 2.1 Packages under `com.xdev.ooms.hr`

| Domain | Status |
|--------|--------|
| Employee / poste / contract (extended) | Done |
| Organization (department, grade, category) | Done |
| Contract amendments + `ContractLegalValidator` | Done |
| Employee documents | Done |
| Work schedules | Done |
| Pointage + anomaly detection | Done |
| Timesheets (+ validate) | Done |
| Overtime rules / requests | Done |
| Leave types, balances, public holidays | Done |
| Legal config (rules, CNSS, tax, SMIG, components, company profile) | Done |
| Payroll engine + payroll variables | Done |
| Payslip enrichment + calculation snapshot/breakdown | Done |
| Salary advances / loans / installments | Done |
| Compliance engine + violations API | Done |
| Dashboard stats API | Done |
| HR Agent (keyword tool router + action log) | Done |
| Finance integration (`PayrollAccountingPort`) | Done |
| Leave-approved notifications | Done |

### 2.2 Cross-module

- `PayrollAccountingPort` / `PayrollAccountingCommand` in **shared-kernel**  
- `PayrollAccountingPortImpl` in **finance** (via `ExpensePort`)  
- Extended `permissions-spec.json` + notification rules  
- Unit tests for engine, CNSS, tax, SMIG, contract legal, attendance anomalies  

### 2.3 Docs in this repo

Located under `docs/hr/`:

- `HR_MODULE_ANALYSIS.md`  
- `HR_ARCHITECTURE.md`  
- `HR_DATABASE_MODEL.md`  
- `HR_PAYROLL_ENGINE.md`  
- `HR_TUNISIAN_LEGAL_CONFIGURATION.md`  
- `HR_API.md`  
- `HR_AGENT.md`  
- `HR_SECURITY.md`  
- `HR_TEST_PLAN.md`  
- `HR_MIGRATION_PLAN.md`  
- `HR_IMPLEMENTATION_SUMMARY.md`  
- **This file:** `HR_STATUS_AND_NEXT.md`  

---

## 3. How to run / smoke-test (backend)

```powershell
# JDK 21
$env:JAVA_HOME = "…\ms-21.0.9"
cd oosm
mvn -pl modules/hr -am test "-Dtest=PayrollEngineTest,SocialSecurityCalculatorTest,IncomeTaxCalculatorTest,SalaryComplianceValidatorTest,ContractLegalValidatorTest,AttendanceAnomalyDetectorTest"
```

After deploy / local boot:

1. Enable tenant module **HR**  
2. `POST /api/hr/legal-rules/seed-defaults`  
3. Configure `CompanyHrLegalProfile` (do **not** assume olive = agricultural)  
4. Create employee → contract → payroll period → generate payslips → validate → check finance post  

---

## 4. What's next (backend priority)

### P0 — Production readiness

| Item | Why |
|------|-----|
| Accountant validation of seeded CNSS / IRPP / SMIG | Rates are **provisional** |
| Tenant-by-tenant `CompanyHrLegalProfile` | Correct CNSS regime / weekly hours |
| Security IDOR tests (cross-tenant, payslip self vs all) | Spec requirement not fully automated |
| Backup + deploy checklist from `HR_MIGRATION_PLAN.md` | Additive Hibernate schema |

### P1 — Payroll completeness

| Item | Why |
|------|-----|
| Mid-month hire/terminate prorating rules | Scenarios 10–11 |
| `PayrollAdjustment` / `PayrollReversal` entities + APIs | Immutable validated payroll |
| Consume advances/loans automatically into `PayrollInputs` on calculate | Closer to full payroll flow |
| CNSS quarterly declaration export | Tunisian compliance reporting |
| Migrate payslip money columns from `Double` → `BigDecimal` | End-to-end precision |

### P2 — Agent / LLM

| Item | Why |
|------|-----|
| Extract `IntentResolver` (keyword + LLM) | Keep tools/permissions unchanged |
| OpenAI **API** key (not ChatGPT Plus) behind feature flag | Cloud LLM planning/explanation |
| Keyword fallback when no key / timeout | Resilience |
| Never allow LLM to write payroll totals | Safety |

Recommended: Spring AI + OpenAI-compatible tool calling over existing `HrAgentToolRegistry`.  
See discussion notes: ChatGPT subscription ≠ API; API is pay-as-you-go.

### P3 — Ops / quality

| Item | Why |
|------|-----|
| Enable Flyway for `hr_*` versioned migrations | Safer than ddl-auto alone |
| `@Version` optimistic locking on payslip / period / legal config | Concurrent edit safety |
| Leave accrual scheduler | Balances over time |
| Broader integration tests for payroll scenarios 1–13 | `HR_TEST_PLAN.md` |

---

## 5. Branch / PR notes

- Branch: `feature/hr-payroll-module`  
- Open PR against your usual base (e.g. `pfe-v2-final`):  
  https://github.com/x-dev-grp/oosm/pull/new/feature/hr-payroll-module  
- Keep frontend PR in sync on the same branch name in `osm-ms-fe`.  

### Authority principle (do not regress)

```text
Legal config (DB, effective-dated)
        ↓
PayrollEngine (Java, BigDecimal)
        ↓
Payslip + snapshot
        ↓
Compliance / Finance / Agent explanation
```

The HR Agent orchestrates. **Java calculates.**
