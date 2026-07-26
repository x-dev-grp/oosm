package com.xdev.ooms.hr.compliance.service;

import com.xdev.ooms.hr.common.enums.ContractStatus;
import com.xdev.ooms.hr.common.enums.ContractType;
import com.xdev.ooms.hr.common.enums.EmployeeStatus;
import com.xdev.ooms.hr.common.enums.WorkRegime;
import com.xdev.ooms.hr.compliance.dto.ComplianceSummaryDto;
import com.xdev.ooms.hr.compliance.entity.HrComplianceViolation;
import com.xdev.ooms.hr.compliance.enums.ComplianceSeverity;
import com.xdev.ooms.hr.compliance.enums.ComplianceViolationStatus;
import com.xdev.ooms.hr.compliance.repository.HrComplianceViolationRepository;
import com.xdev.ooms.hr.contract.entity.EmploymentContract;
import com.xdev.ooms.hr.contract.repository.EmploymentContractRepository;
import com.xdev.ooms.hr.employee.entity.Employee;
import com.xdev.ooms.hr.employee.repository.EmployeeRepository;
import com.xdev.ooms.hr.leave.entity.LeaveBalance;
import com.xdev.ooms.hr.leave.repository.LeaveBalanceRepository;
import com.xdev.ooms.hr.legal.enums.WeeklyRegimeType;
import com.xdev.ooms.hr.legal.validation.ComplianceResult;
import com.xdev.ooms.hr.legal.validation.SalaryComplianceValidator;
import com.xdev.ooms.sharedkernel.utils.AuditHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ComplianceEngine {

    private static final Logger log = LoggerFactory.getLogger(ComplianceEngine.class);
    private static final int CONTRACT_END_DAYS = 30;

    private final EmployeeRepository employeeRepository;
    private final EmploymentContractRepository contractRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final HrComplianceViolationRepository violationRepository;
    private final SalaryComplianceValidator salaryComplianceValidator;

    public ComplianceEngine(
            EmployeeRepository employeeRepository,
            EmploymentContractRepository contractRepository,
            LeaveBalanceRepository leaveBalanceRepository,
            HrComplianceViolationRepository violationRepository,
            SalaryComplianceValidator salaryComplianceValidator
    ) {
        this.employeeRepository = employeeRepository;
        this.contractRepository = contractRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.violationRepository = violationRepository;
        this.salaryComplianceValidator = salaryComplianceValidator;
    }

    @Transactional
    public List<HrComplianceViolation> runScan() {
        LocalDate today = LocalDate.now();
        LocalDateTime detectedAt = LocalDateTime.now();
        List<HrComplianceViolation> produced = new ArrayList<>();
        Set<String> openKeys = new HashSet<>();

        List<Employee> employees = employeeRepository.findAll().stream()
                .filter(e -> !Boolean.TRUE.equals(e.getDeleted()))
                .toList();

        for (Employee employee : employees) {
            if (employee.getStatus() != EmployeeStatus.ACTIVE) {
                continue;
            }
            UUID employeeId = employee.getId();
            EmploymentContract activeContract = contractRepository
                    .findActiveForEmployeeOnDate(employeeId, today)
                    .orElse(null);

            if (activeContract == null) {
                produced.add(upsertViolation(buildViolation(
                        "EMPLOYEE_WITHOUT_CONTRACT",
                        ComplianceSeverity.CRITICAL,
                        "EMPLOYEE",
                        employeeId,
                        employeeId,
                        "Employee without active contract",
                        employeeLabel(employee) + " is active but has no active employment contract.",
                        "Working without a valid contract exposes the company to Tunisian labour-law risk.",
                        "Create or activate an employment contract for this employee.",
                        "CONTRACT_REQUIRED",
                        detectedAt
                ), openKeys));
            } else {
                scanContract(employee, activeContract, today, detectedAt, produced, openKeys);
            }

            if (employee.getCnssMatricule() == null || employee.getCnssMatricule().isBlank()) {
                produced.add(upsertViolation(buildViolation(
                        "EMPLOYEE_WITHOUT_CNSS_NUMBER",
                        ComplianceSeverity.HIGH,
                        "EMPLOYEE",
                        employeeId,
                        employeeId,
                        "Missing CNSS matricule",
                        employeeLabel(employee) + " has no CNSS number.",
                        "CNSS affiliation is mandatory for social security declarations.",
                        "Record the employee CNSS matricule before next payroll.",
                        "CNSS_REQUIRED",
                        detectedAt
                ), openKeys));
            }
        }

        for (LeaveBalance balance : leaveBalanceRepository.findAll()) {
            if (Boolean.TRUE.equals(balance.getDeleted()) || balance.getBalance() == null) {
                continue;
            }
            if (balance.getBalance().compareTo(BigDecimal.ZERO) < 0) {
                UUID employeeId = balance.getEmployee() != null ? balance.getEmployee().getId() : null;
                produced.add(upsertViolation(buildViolation(
                        "NEGATIVE_LEAVE_BALANCE",
                        ComplianceSeverity.WARNING,
                        "LEAVEBALANCE",
                        balance.getId(),
                        employeeId,
                        "Negative leave balance",
                        "Leave balance for type " + balance.getLeaveTypeCode()
                                + " year " + balance.getYear() + " is " + balance.getBalance() + ".",
                        "Negative balances usually indicate over-approved leave or missing accruals.",
                        "Review leave requests and adjust entitlements.",
                        "LEAVE_BALANCE",
                        detectedAt
                ), openKeys));
            }
        }

        log.info("HR compliance scan produced {} open/updated violation(s)", produced.size());
        return produced;
    }

    @Transactional(readOnly = true)
    public ComplianceSummaryDto getSummary() {
        ComplianceSummaryDto summary = new ComplianceSummaryDto();
        long critical = violationRepository.countOpenBySeverity(ComplianceSeverity.CRITICAL);
        long high = violationRepository.countOpenBySeverity(ComplianceSeverity.HIGH);
        long warning = violationRepository.countOpenBySeverity(ComplianceSeverity.WARNING);
        long info = violationRepository.countOpenBySeverity(ComplianceSeverity.INFO);
        long open = critical + high + warning + info;
        long resolved = violationRepository.countByStatusAndIsDeletedFalse(ComplianceViolationStatus.RESOLVED);
        long dismissed = violationRepository.countByStatusAndIsDeletedFalse(ComplianceViolationStatus.DISMISSED);

        summary.setCriticalCount(critical);
        summary.setHighCount(high);
        summary.setWarningCount(warning);
        summary.setInfoCount(info);
        summary.setTotalOpen(open);
        summary.setResolvedCount(resolved);
        summary.setDismissedCount(dismissed);
        summary.setScore(computeScore(critical, high, warning, info));
        return summary;
    }

    private void scanContract(
            Employee employee,
            EmploymentContract contract,
            LocalDate today,
            LocalDateTime detectedAt,
            List<HrComplianceViolation> produced,
            Set<String> openKeys
    ) {
        UUID employeeId = employee.getId();
        UUID contractId = contract.getId();

        if (contract.getContractType() == ContractType.CDD && contract.getCddLegalReason() == null) {
            produced.add(upsertViolation(buildViolation(
                    "CDD_WITHOUT_LEGAL_REASON",
                    ComplianceSeverity.HIGH,
                    "CONTRACT",
                    contractId,
                    employeeId,
                    "CDD without legal reason",
                    "Contract " + nullSafe(contract.getContractNumber())
                            + " for " + employeeLabel(employee) + " is CDD without a legal reason.",
                    "Tunisian labour law requires a documented legal ground for fixed-term contracts.",
                    "Set a valid CDD legal reason and supporting details.",
                    "CDD_LEGAL_REASON",
                    detectedAt
            ), openKeys));
        }

        if (contract.getContractType() == ContractType.CDD
                && contract.getEndDate() != null
                && contract.getEndDate().isBefore(today)
                && contract.getStatus() == ContractStatus.ACTIVE) {
            produced.add(upsertViolation(buildViolation(
                    "EXPIRED_CDD_STILL_ACTIVE",
                    ComplianceSeverity.CRITICAL,
                    "CONTRACT",
                    contractId,
                    employeeId,
                    "Expired CDD still active",
                    "CDD " + nullSafe(contract.getContractNumber())
                            + " ended on " + contract.getEndDate() + " but remains ACTIVE.",
                    "An expired CDD left active may be requalified as CDI and create liabilities.",
                    "Terminate, renew, or convert the contract immediately.",
                    "CDD_END_DATE",
                    detectedAt
            ), openKeys));
        }

        if (contract.getEndDate() != null
                && !contract.getEndDate().isBefore(today)
                && !contract.getEndDate().isAfter(today.plusDays(CONTRACT_END_DAYS))) {
            produced.add(upsertViolation(buildViolation(
                    "CONTRACT_END_APPROACHING",
                    ComplianceSeverity.WARNING,
                    "CONTRACT",
                    contractId,
                    employeeId,
                    "Contract ending within 30 days",
                    "Contract " + nullSafe(contract.getContractNumber())
                            + " ends on " + contract.getEndDate() + ".",
                    "Early planning avoids gaps in coverage and unlawful extensions.",
                    "Plan renewal, conversion, or termination before the end date.",
                    "CONTRACT_END",
                    detectedAt
            ), openKeys));
        }

        BigDecimal salary = contract.getBaseSalary();
        if (salary == null && contract.getSalary() != null) {
            salary = BigDecimal.valueOf(contract.getSalary());
        }
        if (salary != null) {
            try {
                WeeklyRegimeType regime = mapRegime(employee.getWorkRegime());
                ComplianceResult result = salaryComplianceValidator.validate(salary, regime, today);
                if (!result.isCompliant()) {
                    produced.add(upsertViolation(buildViolation(
                            "SALARY_BELOW_MINIMUM",
                            ComplianceSeverity.CRITICAL,
                            "CONTRACT",
                            contractId,
                            employeeId,
                            "Salary below minimum wage",
                            result.getMessage() != null
                                    ? result.getMessage()
                                    : "Base salary is below applicable minimum.",
                            "Paying below SMIG/SMAG is a legal and financial risk.",
                            "Raise base salary to at least " + result.getApplicableMinimum() + ".",
                            "MINIMUM_WAGE",
                            detectedAt
                    ), openKeys));
                }
            } catch (RuntimeException ex) {
                log.warn("Salary compliance check skipped for contract {}: {}", contractId, ex.getMessage());
            }
        }
    }

    private HrComplianceViolation upsertViolation(HrComplianceViolation candidate, Set<String> openKeys) {
        String key = candidate.getCode() + ":" + candidate.getEntityId();
        openKeys.add(key);
        if (violationRepository.existsOpenByCodeAndEntity(candidate.getCode(), candidate.getEntityId())) {
            return candidate;
        }
        AuditHelper.applyAuditOnCreate(candidate);
        return violationRepository.save(candidate);
    }

    private HrComplianceViolation buildViolation(
            String code,
            ComplianceSeverity severity,
            String entityType,
            UUID entityId,
            UUID employeeId,
            String title,
            String description,
            String whyItMatters,
            String recommendedAction,
            String relatedRuleCode,
            LocalDateTime detectedAt
    ) {
        HrComplianceViolation v = new HrComplianceViolation();
        v.setCode(code);
        v.setSeverity(severity);
        v.setEntityType(entityType);
        v.setEntityId(entityId);
        v.setEmployeeId(employeeId);
        v.setTitle(title);
        v.setDescription(description);
        v.setWhyItMatters(whyItMatters);
        v.setRecommendedAction(recommendedAction);
        v.setRelatedRuleCode(relatedRuleCode);
        v.setStatus(ComplianceViolationStatus.OPEN);
        v.setDetectedAt(detectedAt);
        return v;
    }

    private static int computeScore(long critical, long high, long warning, long info) {
        long penalty = critical * 25L + high * 10L + warning * 4L + info;
        int score = (int) Math.max(0, 100 - penalty);
        return Math.min(100, score);
    }

    private static WeeklyRegimeType mapRegime(WorkRegime workRegime) {
        if (workRegime == null) {
            return WeeklyRegimeType.HOURS_48;
        }
        return switch (workRegime) {
            case HOURS_40 -> WeeklyRegimeType.HOURS_40;
            case HOURS_48, AGRICULTURAL -> WeeklyRegimeType.HOURS_48;
        };
    }

    private static String employeeLabel(Employee employee) {
        return nullSafe(employee.getFirstName()) + " " + nullSafe(employee.getLastName());
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
