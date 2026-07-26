# HR Security — OSM 2.0

## Model

- OAuth2 / JWT resource server  
- Authorities: `MODULE:ENTITY:ACTION`  
- Catalog: `modules/security/src/main/resources/permissions/permissions-spec.json`  
- Synced at startup via permission catalog sync  
- Tenant isolation: `TenantContext` + repository filters + module path gate  

---

## HR role presets

| Role | Intent |
|------|--------|
| `HR_CLERK` | Daily ops: employees, attendance, leave submit |
| `HR_MANAGER` | Contracts, leave approve, org masters |
| `HR_PAYROLL_OFFICER` | Payroll calculate/validate/pay + legal READ |
| `HR_ADMIN` | Full HR including legal config, compliance, agent |

Admins `ADMIN` / `OOSMADMIN` bypass via `HrPermissionSupport`.

---

## New / extended entities (permissions)

Organization, legal config, timesheets, overtime, leave types, advances, loans, documents, compliance, agent, payroll variables — see `permissions-spec.json` profiles:

- `HR_MASTER`, `HR_EMPLOYEE`, `HR_CONTRACT`, `HR_ATTENDANCE`, `HR_LEAVE`
- `HR_PAYROLL_PERIOD`, `HR_PAYSLIP`
- `HR_LEGAL_CONFIG`
- `HR_COMPLIANCE_READ` / `HR_COMPLIANCE_ADMIN`
- `HR_AGENT_USE`

---

## Mandatory backend checks

- Controllers/services use `HrPermissionSupport.requireAction` for sensitive workflows (approve, calculate, validate, agent writes)
- Soft-delete only for employees with history
- Locked payroll periods reject updates
- Payslip self vs all: enforce via permissions (`PAYSLIP` READ); finer self-only scoping is a recommended hardening follow-up
- Agent CRITICAL actions require confirmation flag

---

## Audit

Sensitive changes flow through:

- BaseEntity audit fields  
- `HrAgentActionLog` for AI/tool actions  
- Payslip `calculationSnapshot` for payroll reproducibility  
- Finance `financeReference` for posted payroll  

Do not log secrets/tokens.
