# HR Database Model — OSM 2.0

Schema is managed primarily by Hibernate (`ddl-auto=update`). Tables use prefix `hr_`. All entities inherit BaseEntity columns: `id`, `tenant_id`, `is_deleted`, `created_by`, `created_date`, `last_modified_by`, `last_modified_date`, optional QR fields.

---

## Core tables (pre-existing, extended)

| Table | Notes |
|-------|-------|
| `hr_employee` | + employeeNumber, gender, maritalStatus, children, tax/rib, terminationDate, FKs to grade/category/department |
| `hr_poste` | Positions |
| `hr_employment_contract` | + contractNumber, cddLegalReason, probation, weeklyHours, baseSalary (BigDecimal), salary synced |
| `hr_pointage` | + source, minute fields, validated, anomalyCodes |
| `hr_leave_request` | Existing leave workflow |
| `hr_payroll_period` | + financePosted, financeReference |
| `hr_payslip` | + taxableSalary, cnssBase, employerCost, otherDeductions, calculationSnapshot, calculationBreakdown |

---

## Organization

| Table | Purpose |
|-------|---------|
| `hr_department` | Hierarchical departments |
| `hr_grade` | Grades |
| `hr_employee_category` | Categories |
| `hr_contract_amendment` | Historical contract changes |
| `hr_employee_document` | Document metadata |

---

## Time & leave

| Table | Purpose |
|-------|---------|
| `hr_work_schedule` | Schedules / shifts |
| `hr_timesheet` / `hr_timesheet_line` | Validated attendance for payroll |
| `hr_overtime_rule` / `hr_overtime_request` | Configurable OT |
| `hr_leave_type` | Configurable leave types |
| `hr_leave_balance` | Balances per employee/year |
| `hr_public_holiday` | Holidays |

---

## Legal & payroll config

| Table | Purpose |
|-------|---------|
| `hr_legal_rule` | Generic effective-dated rules |
| `hr_company_legal_profile` | Tenant HR legal profile |
| `hr_social_security_config` | CNSS / CSS rates |
| `hr_tax_configuration` / `hr_tax_bracket` | IRPP brackets |
| `hr_minimum_wage_rule` | SMIG / profiles |
| `hr_salary_component` | Earnings / deductions catalog |
| `hr_payroll_variable` | Monthly variables |

---

## Advances, loans, compliance, agent

| Table | Purpose |
|-------|---------|
| `hr_salary_advance` | Advances |
| `hr_employee_loan` / `hr_loan_installment` | Loans |
| `hr_compliance_violation` | Compliance findings |
| `hr_agent_action_log` | Agent audit |

---

## Indexes / constraints (logical)

- Tenant + soft-delete filter on all reads
- CIN / CNSS uniqueness enforced in `HrBusinessLinkageService` (per tenant)
- Active contract overlap checks in linkage service
- Locked payroll periods (`PAID` / `CLOSED`) reject mutation

---

## Migration notes

- Flyway is **disabled** in current deploy config
- Prefer additive columns; never recreate tables with production data
- Seed provisional Tunisian rates via `POST /api/hr/legal-rules/seed-defaults`
- See `HR_MIGRATION_PLAN.md`
