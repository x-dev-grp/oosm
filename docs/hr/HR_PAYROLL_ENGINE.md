# HR Payroll Engine — OSM 2.0

**Package:** `com.xdev.ooms.hr.payroll.engine`  
**Entry:** `PayrollEngine.calculate(baseSalary, inputs, periodEnd, workRegime)`

---

## Principle

```text
LegalConfigurationService (effective-dated rates)
        ↓
PayrollEngine (BigDecimal, deterministic)
        ↓
PayrollResult (lines + breakdown + legal snapshot JSON)
        ↓
Payslip persistence (legacy Double columns + snapshot TEXT)
```

No CNSS/tax/SMIG constants inside calculators.

---

## Pipeline

1. **GrossSalaryCalculator** — base + overtime + bonuses + other earnings  
2. **AbsenceDeductionCalculator** — absence deductions  
3. **SocialSecurityCalculator** — employee/employer CNSS + CSS from `SocialSecurityConfiguration`  
4. **IncomeTaxCalculator** — progressive brackets from `TaxConfiguration` / `TaxBracket`  
5. **NetSalaryCalculator** — gross − CNSS − tax − CSS − advances − loans − other  
6. **EmployerCostCalculator** — gross path + employer contributions  
7. **PayrollValidator** — minimum-wage anomalies via `SalaryComplianceValidator`

---

## Money

`HrMoney` — `BigDecimal`, `HALF_UP`, money scale 3/2. Never `double`/`float` inside the engine.

---

## Snapshot / explainability

On payslip enrichment (`HrBusinessLinkageService`):

- `calculationSnapshot` — JSON of legal config versions used  
- `calculationBreakdown` — ordered steps (base, gross, CNSS, tax, net, employer cost)  
- Used by HR Agent `payroll.explainEmployee` style answers

---

## Wiring

| Caller | Behavior |
|--------|----------|
| Payslip create/update | Engine calculates from contract base salary + inputs |
| `HrPayrollCalculator` | Spring `@Component` facade over engine (no hardcoded rates) |
| Missing config | `IllegalStateException` → seed via `/api/hr/legal-rules/seed-defaults` |

---

## Locking

Once payroll period is `PAID` / `CLOSED`, period and related payslips must not be silently rewritten. Corrections require future adjustment/reversal flows (partially scaffolded; full reversal entity set is follow-up work).

---

## Tests

```text
SocialSecurityCalculatorTest
IncomeTaxCalculatorTest
PayrollEngineTest
MinimumWageValidatorTest / SalaryComplianceValidatorTest
```

Run:

```powershell
$env:JAVA_HOME = "…\ms-21.0.9"
mvn test -pl modules/hr "-Dtest=PayrollEngineTest,SocialSecurityCalculatorTest,IncomeTaxCalculatorTest,SalaryComplianceValidatorTest"
```
