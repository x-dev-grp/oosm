# HR Agent — OSM 2.0

## Architecture

```text
User prompt
    ↓
HrAgentController POST /api/hr/agent/query
    ↓
HrAgentService (keyword / intent router — no LLM payroll math)
    ↓
HrAgentToolRegistry → existing services / ComplianceEngine / repositories
    ↓
Structured AgentResponse + HrAgentActionLog
```

**Java calculates. The agent explains and orchestrates.**

A real LLM can later replace the keyword router while keeping the same tool registry.

---

## Risk levels

| Level | Behavior |
|-------|----------|
| READ | Runs if user has tool permission |
| CONTROLLED_WRITE | Requires `confirmed=true` + elevated permission |
| CRITICAL | Requires confirmation + APPROVE-class permission |

---

## Tools (implemented)

| Tool | Intent examples |
|------|-----------------|
| `employee.search` / `employee.get` | Find employee |
| `employee.listMissingCnss` | Missing CNSS |
| `contract.expiring` | Contracts ending soon |
| `leave.pending` | Pending leave |
| `payroll.get` / `payroll.anomalies` | Period status / anomalies |
| `compliance.summary` / `compliance.violations` | Compliance |
| `reports.payrollCost` | Employer / payroll cost from payslips |

---

## Security

- Permission resource: `HRAGENT`
- Every invocation logged: prompt, intent, tool, params, result, risk, confirmation
- Never accepts client-supplied net salary as truth

---

## Frontend

Route `/hr/agent` — prompt + confirmation checkbox for write/critical actions.
