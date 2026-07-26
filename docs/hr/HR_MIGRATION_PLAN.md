# HR Migration Plan — OSM 2.0

## Context

Existing HR tables (`hr_employee`, `hr_poste`, `hr_employment_contract`, `hr_pointage`, `hr_leave_request`, `hr_payroll_period`, `hr_payslip`) already hold data. Flyway is disabled; Hibernate `ddl-auto=update` adds columns/tables.

**Do not destroy existing data.**

---

## Strategy

1. **Additive schema** — new columns/tables only  
2. **Dual fields** where needed (e.g. String `department` + `department_ref_id`; Double `salary` + BigDecimal `baseSalary`)  
3. **Seed legal config** after deploy: `POST /api/hr/legal-rules/seed-defaults`  
4. **Backfill** employee numbers / baseSalary from salary when null (optional SQL)  
5. **Rollback** — drop new tables/columns only if unused; never drop legacy columns in first release  

---

## Mapping

| Old | New | Action |
|-----|-----|--------|
| `Employee.department` (String) | `Department` entity | Keep string; optional FK `departmentRef` |
| `Employee.jobTitle` | Poste / contract | Keep as display fallback |
| `EmploymentContract.salary` (Double) | `baseSalary` (BigDecimal) | Sync both on save |
| Hardcoded `HrPayrollCalculator` rates | `SocialSecurityConfiguration` / tax / SMIG | Engine uses DB; seed defaults |
| Payslip fixed columns | + snapshot/breakdown/employerCost | Additive |
| — | All Phase 1–5 `hr_*` tables | Created by Hibernate |

---

## Compatibility risks

| Risk | Mitigation |
|------|------------|
| Missing legal seed → payroll fails | Document seed endpoint; fail clearly |
| Old FE still computing CNSS | Marked provisional; display BE values |
| Permission catalog not synced | Restart app to sync `permissions-spec.json` |
| Large tenant payroll | Batch generate payslips already loops employees |

---

## Rollback

1. Disable new Angular routes via menu if needed  
2. New tables unused → safe to leave  
3. Do **not** revert payslip snapshots once payroll validated in production  

---

## Checklist

- [ ] Backup DB before production deploy  
- [ ] Deploy monolith with HR module  
- [ ] Confirm permission sync  
- [ ] Seed legal defaults per tenant  
- [ ] Configure `CompanyHrLegalProfile` (activity ≠ assume agricultural)  
- [ ] Validate provisional rates with accountant  
- [ ] Smoke: employee → contract → period → payslip PDF → validate → finance post  
