# Tunisian Legal Configuration — OSM 2.0 HR

## Rule

**Do not hardcode** CNSS rates, IRPP brackets, SMIG, overtime multipliers, or leave allowances in business logic.

All statutory values are:

- stored in DB entities  
- **effective-dated** (`effectiveFrom` / `effectiveTo`)  
- versioned where applicable  
- tenant-aware  
- resolved by `LegalConfigurationService` for the payroll period date  

---

## Entities

| Entity | Role |
|--------|------|
| `LegalRule` | Generic rules by `LegalRuleCategory` |
| `CompanyHrLegalProfile` | Activity, CNSS employer no., weekly regime, sector — **does not assume olive = agricultural** |
| `SocialSecurityConfiguration` | Employee/employer CNSS, CSS, accident rate |
| `TaxConfiguration` + `TaxBracket` | Progressive IRPP |
| `MinimumWageRule` | SMIG / profiles for 40h / 48h / other |
| `SalaryComponent` | Catalog of earnings/deductions |
| `OvertimeRule` | OT multipliers by day type |
| `LeaveTypeConfig` | Leave policies |

---

## Provisional seed values

`POST /api/hr/legal-rules/seed-defaults` (`LegalConfigurationSeedService`) inserts **provisional** Tunisian parameters for empty tenants, including historically documented:

| Item | Provisional value |
|------|-------------------|
| CNSS employee | 9.68% |
| CNSS employer | 17.07% |
| CSS | 0.5% |
| SMIG 48h monthly | 528.32 |
| SMIG 40h monthly | 448.238 |
| IRPP brackets (monthly simplified) | 0–1500 0%; 1500–5000 15%; 5000–10000 25%; 10000–20000 30%; 20000+ 35% |

> **These must be validated by a Tunisian accountant / payroll specialist before production use.** Rates change; the system is designed so you update configuration rows, not Java.

---

## Company activity classification

`BusinessActivityType`:

- `OLIVE_GROWING`
- `INDUSTRIAL_PROCESSING`
- `PACKAGING`
- `LOGISTICS`
- `ADMINISTRATION`
- `OTHER`

Configure CNSS regime and minimum-wage profile per tenant profile — do not infer from “olive oil mill” alone.

---

## Contract legal rules

`ContractLegalValidator` violation codes:

- `CDD_WITHOUT_LEGAL_REASON`
- `INVALID_CONTRACT_DATES`
- `INVALID_PROBATION_PERIOD`
- `CONTRACT_WITHOUT_SALARY`
- `CDD_MISSING_END_DATE`

CDD / seasonal / temporary contracts require `CddLegalReason`.

---

## Frontend

`tunisia-hr.constants.ts` and preview utils are **not** the payroll authority. UI should display backend-calculated payslip fields.
