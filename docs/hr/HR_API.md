# HR API — OSM 2.0

Base: `/api/hr`  
Auth: Bearer JWT  
Tenant: from JWT (`TenantContext`)  
Module gate: tenant must enable `HR`

Standard CRUD (via `BaseControllerImpl`):

| Method | Path | Notes |
|--------|------|-------|
| GET | `/{resource}/fetch/{id}` | |
| GET | `/{resource}/fetchAll` | |
| GET | `/{resource}/fetchAllPageable` | |
| POST | `/{resource}` | Create |
| PUT | `/{resource}` | Update |
| DELETE | `/{resource}/delete/{id}` | Soft delete |
| POST | `/{resource}/advanced/search` | Dashboard search |

Nested DTOs: `{ "employee": { "id": "…" } }` — never flat `employeeId` in API body.

---

## Resources

| Resource name | Path |
|---------------|------|
| EMPLOYEE | `/employees` |
| POSTE | `/postes` |
| DEPARTMENT | `/departments` |
| GRADE | `/grades` |
| EMPLOYEECATEGORY | `/employee-categories` |
| CONTRACT | `/contracts` |
| CONTRACTAMENDMENT | `/contract-amendments` |
| EMPLOYEEDOCUMENT | `/employee-documents` |
| WORKSCHEDULE | `/work-schedules` |
| POINTAGE | `/pointages` |
| TIMESHEET | `/timesheets` |
| OVERTIMERULE | `/overtime-rules` |
| OVERTIMEREQUEST | `/overtime-requests` |
| LEAVEREQUEST | `/leave-requests` |
| LEAVETYPE | `/leave-types` |
| LEAVEBALANCE | `/leave-balances` |
| PUBLICHOLIDAY | `/public-holidays` |
| LEGALRULE | `/legal-rules` |
| COMPANYLEGALPROFILE | `/company-legal-profile` |
| SOCIALSECURITYCONFIG | `/social-security-configs` |
| TAXCONFIGURATION | `/tax-configurations` |
| TAXBRACKET | `/tax-brackets` |
| MINIMUMWAGERULE | `/minimum-wage-rules` |
| SALARYCOMPONENT | `/salary-components` |
| PAYROLLPERIOD | `/payroll-periods` |
| PAYROLLVARIABLE | `/payroll-variables` |
| PAYSLIP | `/payslips` |
| SALARYADVANCE | `/salary-advances` |
| EMPLOYEELOAN | `/employee-loans` |
| COMPLIANCE | `/compliance-violations` |

---

## Special endpoints

| Method | Path | Permission | Purpose |
|--------|------|------------|---------|
| POST | `/legal-rules/seed-defaults` | LEGALRULE write | Seed provisional Tunisian config |
| PATCH | `/leave-requests/{id}/approve` | APPROVE | Approve leave |
| PATCH | `/leave-requests/{id}/reject` | REJECT | Reject leave |
| POST | `/timesheets/{id}/validate` | VALIDATE | Validate timesheet |
| PATCH | `/overtime-requests/{id}/approve` | APPROVE | Approve OT |
| POST | `/payroll-periods/{id}/generate-payslips` or calculate action | CALCULATE | Generate payslips |
| GET | `/payslips/{id}/pdf` | GEN_PDF | Payslip PDF |
| GET | `/dashboard/stats` | EMPLOYEE READ | KPI stats |
| GET | `/compliance/summary` | COMPLIANCE READ | Score |
| GET | `/compliance/violations` | COMPLIANCE READ | Open violations |
| POST | `/compliance/scan` | COMPLIANCE REPORT | Rescan |
| POST | `/agent/query` | HRAGENT | Agent tool router |

Exact payroll calculate/advance paths follow existing `PayrollPeriodController` custom mappings — see controller for generate/advance status.

---

## Response wrappers

- Entity lists: `apiDTOs.ApiResponse<E, Dto>` → `{ success, message, data: [] }`
- Non-entity (dashboard, agent, compliance summary): `communicator…ApiResponse<T>` → `{ success, message, data: T }`
