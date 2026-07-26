# HR Test Plan — OSM 2.0

## Unit tests implemented

| Test | Coverage |
|------|----------|
| `SocialSecurityCalculatorTest` | CNSS/CSS from config rates |
| `IncomeTaxCalculatorTest` | Progressive brackets |
| `PayrollEngineTest` | End-to-end engine with in-memory config |
| `SalaryComplianceValidatorTest` / `MinimumWageValidatorTest` | SMIG compliance |
| `ContractLegalValidatorTest` | CDD reason, dates, salary |
| `AttendanceAnomalyDetectorTest` | Missing punches, late, early, duplicate |

Run (JDK 21):

```powershell
$env:JAVA_HOME = "C:\Users\ismail.mansouri\.jdks\ms-21.0.9"
cd "c:\OSM 2.0\oosm"
mvn test -pl modules/hr "-Dtest=SocialSecurityCalculatorTest,IncomeTaxCalculatorTest,PayrollEngineTest,SalaryComplianceValidatorTest,ContractLegalValidatorTest,AttendanceAnomalyDetectorTest"
```

**Status:** Passing as of implementation date.

---

## Payroll scenarios (manual / next automated)

| # | Scenario | Expected |
|---|----------|----------|
| 1 | Standard CDI, no OT/absence | Gross→CNSS→tax→net from config |
| 2 | Approved overtime | OT in gross per inputs |
| 3 | Unpaid absence | Deduction applied |
| 4 | Paid leave | No improper deduction |
| 5 | Salary advance | Deduction when approved/consumed |
| 6 | Loan installment | Deduction |
| 7 | Bonus variable | Per component taxable/CNSS flags |
| 8 | Below minimum | Compliance / anomaly |
| 9 | Mid-year rate change | Effective-dated config for period end |
| 10–11 | Hire/terminate mid-month | Prorate (partial — verify business rule) |
| 12 | Payroll rerun before validate | Idempotent regenerate |
| 13 | Modify after PAID/CLOSED | Rejected |

---

## Contract scenarios

Covered partially by `ContractLegalValidatorTest`; extend for amendments and probation edge cases.

---

## Security tests (recommended next)

- Cross-tenant employee/payslip IDOR  
- Unauthorized payroll validate  
- Agent critical without confirmation  
- File/document access by tenant  

---

## Frontend

Angular development build succeeded for HR module extensions. Add e2e for compliance scan + agent query when CI has browser tooling.
