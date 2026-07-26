package com.xdev.ooms.hr.agent.service;

import com.xdev.ooms.hr.agent.enums.AgentRiskLevel;
import com.xdev.ooms.hr.common.HrPermissionSupport;
import com.xdev.ooms.hr.compliance.enums.ComplianceViolationStatus;
import com.xdev.ooms.hr.compliance.repository.HrComplianceViolationRepository;
import com.xdev.ooms.hr.compliance.service.ComplianceEngine;
import com.xdev.ooms.hr.contract.entity.EmploymentContract;
import com.xdev.ooms.hr.contract.repository.EmploymentContractRepository;
import com.xdev.ooms.hr.employee.entity.Employee;
import com.xdev.ooms.hr.employee.repository.EmployeeRepository;
import com.xdev.ooms.hr.leave.entity.LeaveRequest;
import com.xdev.ooms.hr.leave.repository.LeaveRequestRepository;
import com.xdev.ooms.hr.payroll.entity.PayrollPeriod;
import com.xdev.ooms.hr.payroll.repository.PayrollPeriodRepository;
import com.xdev.ooms.hr.payslip.entity.Payslip;
import com.xdev.ooms.hr.payslip.repository.PayslipRepository;
import com.xdev.ooms.sharedkernel.models.Action;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Deterministic tool registry for the HR Agent.
 * Tools call existing repositories/services — no LLM invocation.
 * A real LLM router can be plugged later by mapping intents to these tool names.
 */
@Component
public class HrAgentToolRegistry {

    public record ToolDefinition(
            String name,
            AgentRiskLevel riskLevel,
            String requiredResource,
            Action requiredAction,
            Function<Map<String, String>, Object> handler
    ) {
    }

    private final Map<String, ToolDefinition> tools = new LinkedHashMap<>();

    public HrAgentToolRegistry(
            EmployeeRepository employeeRepository,
            EmploymentContractRepository contractRepository,
            LeaveRequestRepository leaveRequestRepository,
            PayrollPeriodRepository payrollPeriodRepository,
            PayslipRepository payslipRepository,
            ComplianceEngine complianceEngine,
            HrComplianceViolationRepository violationRepository
    ) {
        register("employee.search", AgentRiskLevel.READ, "EMPLOYEE", Action.READ, params -> {
            String q = params.getOrDefault("q", params.getOrDefault("query", ""));
            if (q.isBlank()) {
                return List.of();
            }
            return employeeRepository.searchByKeyword(q.trim()).stream().map(this::employeeBrief).toList();
        });

        register("employee.get", AgentRiskLevel.READ, "EMPLOYEE", Action.READ, params -> {
            UUID id = UUID.fromString(require(params, "id"));
            Employee e = employeeRepository.findByIdAndIsDeletedFalse(id)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));
            return employeeBrief(e);
        });

        register("employee.listMissingCnss", AgentRiskLevel.READ, "EMPLOYEE", Action.READ, params ->
                employeeRepository.findActiveMissingCnss().stream().map(this::employeeBrief).toList());

        register("contract.expiring", AgentRiskLevel.READ, "CONTRACT", Action.READ, params -> {
            LocalDate from = LocalDate.now();
            LocalDate to = from.plusDays(30);
            return contractRepository.findExpiringBetween(from, to).stream().map(this::contractBrief).toList();
        });

        register("leave.pending", AgentRiskLevel.READ, "LEAVEREQUEST", Action.READ, params ->
                leaveRequestRepository.findPending().stream().map(this::leaveBrief).toList());

        register("payroll.get", AgentRiskLevel.READ, "PAYROLLPERIOD", Action.READ, params -> {
            PayrollPeriod period = resolvePeriod(payrollPeriodRepository, params);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", period.getId());
            result.put("year", period.getYear());
            result.put("month", period.getMonth());
            result.put("status", period.getStatus());
            result.put("financePosted", period.getFinancePosted());
            result.put("financeReference", period.getFinanceReference());
            result.put("payslipCount", payslipRepository.findByPayrollPeriod_IdAndIsDeletedFalse(period.getId()).size());
            return result;
        });

        register("payroll.anomalies", AgentRiskLevel.READ, "PAYROLLPERIOD", Action.READ, params -> {
            PayrollPeriod period = resolvePeriod(payrollPeriodRepository, params);
            List<Payslip> anomalies = payslipRepository.findAnomaliesByPeriod(period.getId());
            return anomalies.stream().map(p -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("payslipId", p.getId());
                m.put("employeeId", p.getEmployee() != null ? p.getEmployee().getId() : null);
                m.put("grossSalary", p.getGrossSalary());
                m.put("netSalary", p.getNetSalary());
                return m;
            }).toList();
        });

        register("compliance.summary", AgentRiskLevel.READ, "COMPLIANCE", Action.READ, params ->
                complianceEngine.getSummary());

        register("compliance.violations", AgentRiskLevel.READ, "COMPLIANCE", Action.READ, params ->
                violationRepository.findByStatusAndIsDeletedFalse(ComplianceViolationStatus.OPEN).stream()
                        .map(v -> {
                            Map<String, Object> m = new LinkedHashMap<>();
                            m.put("id", v.getId());
                            m.put("code", v.getCode());
                            m.put("severity", v.getSeverity());
                            m.put("title", v.getTitle());
                            m.put("employeeId", v.getEmployeeId());
                            return m;
                        }).toList());

        register("reports.payrollCost", AgentRiskLevel.READ, "PAYROLLPERIOD", Action.REPORT, params -> {
            PayrollPeriod period = resolvePeriod(payrollPeriodRepository, params);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("periodId", period.getId());
            m.put("label", period.getYear() + "-" + period.getMonth());
            m.put("gross", payslipRepository.sumGrossByPeriod(period.getId()));
            m.put("net", payslipRepository.sumNetByPeriod(period.getId()));
            m.put("employerCost", payslipRepository.sumEmployerCostByPeriod(period.getId()));
            return m;
        });
    }

    public ToolDefinition get(String name) {
        return tools.get(name);
    }

    public Map<String, ToolDefinition> all() {
        return Map.copyOf(tools);
    }

    public void checkPermission(ToolDefinition tool) {
        HrPermissionSupport.requireAction(tool.requiredResource(), tool.requiredAction());
    }

    private void register(
            String name,
            AgentRiskLevel risk,
            String resource,
            Action action,
            Function<Map<String, String>, Object> handler
    ) {
        tools.put(name, new ToolDefinition(name, risk, resource, action, handler));
    }

    private PayrollPeriod resolvePeriod(PayrollPeriodRepository repo, Map<String, String> params) {
        if (params.containsKey("periodId")) {
            return repo.findByIdAndIsDeletedFalse(UUID.fromString(params.get("periodId")))
                    .orElseThrow(() -> new IllegalArgumentException("Payroll period not found"));
        }
        YearMonth ym = YearMonth.now();
        int year = params.containsKey("year") ? Integer.parseInt(params.get("year")) : ym.getYear();
        int month = params.containsKey("month") ? Integer.parseInt(params.get("month")) : ym.getMonthValue();
        return repo.findByYearAndMonthAndIsDeletedFalse(year, month)
                .orElseThrow(() -> new IllegalArgumentException("No payroll period for " + year + "-" + month));
    }

    private Map<String, Object> employeeBrief(Employee e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("firstName", e.getFirstName());
        m.put("lastName", e.getLastName());
        m.put("employeeNumber", e.getEmployeeNumber());
        m.put("status", e.getStatus());
        m.put("cnssMatricule", e.getCnssMatricule());
        return m;
    }

    private Map<String, Object> contractBrief(EmploymentContract c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("contractNumber", c.getContractNumber());
        m.put("contractType", c.getContractType());
        m.put("endDate", c.getEndDate());
        m.put("employeeId", c.getEmployee() != null ? c.getEmployee().getId() : null);
        return m;
    }

    private Map<String, Object> leaveBrief(LeaveRequest l) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", l.getId());
        m.put("employeeId", l.getEmployee() != null ? l.getEmployee().getId() : null);
        m.put("startDate", l.getStartDate());
        m.put("endDate", l.getEndDate());
        m.put("status", l.getStatus());
        m.put("leaveType", l.getLeaveType());
        return m;
    }

    private static String require(Map<String, String> params, String key) {
        String value = params.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing parameter: " + key);
        }
        return value;
    }
}
