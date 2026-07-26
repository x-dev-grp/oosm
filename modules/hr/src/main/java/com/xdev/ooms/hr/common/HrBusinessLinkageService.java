package com.xdev.ooms.hr.common;

import com.xdev.ooms.hr.common.enums.AttendanceStatus;
import com.xdev.ooms.hr.common.enums.ContractStatus;
import com.xdev.ooms.hr.common.enums.ContractType;
import com.xdev.ooms.hr.common.enums.EmployeeStatus;
import com.xdev.ooms.hr.common.enums.LeaveStatus;
import com.xdev.ooms.hr.common.enums.PayrollPeriodStatus;
import com.xdev.ooms.hr.common.enums.PayslipStatus;
import com.xdev.ooms.hr.contract.entity.EmploymentContract;
import com.xdev.ooms.hr.contract.repository.EmploymentContractRepository;
import com.xdev.ooms.hr.contract.validation.ContractLegalValidator;
import com.xdev.ooms.hr.employee.entity.Employee;
import com.xdev.ooms.hr.employee.repository.EmployeeRepository;
import com.xdev.ooms.hr.leave.entity.LeaveRequest;
import com.xdev.ooms.hr.leave.repository.LeaveRequestRepository;
import com.xdev.ooms.hr.legal.enums.WeeklyRegimeType;
import com.xdev.ooms.hr.payroll.engine.PayrollContextBuilder;
import com.xdev.ooms.hr.payroll.engine.PayrollEngine;
import com.xdev.ooms.hr.payroll.engine.PayrollInputs;
import com.xdev.ooms.hr.payroll.engine.PayrollResult;
import com.xdev.ooms.hr.payroll.entity.PayrollPeriod;
import com.xdev.ooms.hr.payroll.repository.PayrollPeriodRepository;
import com.xdev.ooms.hr.payslip.entity.Payslip;
import com.xdev.ooms.hr.payslip.repository.PayslipRepository;
import com.xdev.ooms.hr.pointage.entity.Pointage;
import com.xdev.ooms.hr.pointage.repository.PointageRepository;
import com.xdev.ooms.hr.poste.entity.Poste;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class HrBusinessLinkageService {

    private static final EnumSet<PayrollPeriodStatus> LOCKED_PAYROLL_STATUSES = EnumSet.of(
            PayrollPeriodStatus.PAID,
            PayrollPeriodStatus.CLOSED
    );

    private final EmploymentContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;
    private final PointageRepository pointageRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PayslipRepository payslipRepository;
    private final PayrollPeriodRepository payrollPeriodRepository;
    private final ContractLegalValidator contractLegalValidator;
    private final PayrollEngine payrollEngine;
    private final PayrollContextBuilder payrollContextBuilder;
    private final ObjectMapper objectMapper;

    public HrBusinessLinkageService(
            EmploymentContractRepository contractRepository,
            EmployeeRepository employeeRepository,
            PointageRepository pointageRepository,
            LeaveRequestRepository leaveRequestRepository,
            PayslipRepository payslipRepository,
            PayrollPeriodRepository payrollPeriodRepository,
            ContractLegalValidator contractLegalValidator,
            PayrollEngine payrollEngine,
            PayrollContextBuilder payrollContextBuilder,
            ObjectMapper objectMapper
    ) {
        this.contractRepository = contractRepository;
        this.employeeRepository = employeeRepository;
        this.pointageRepository = pointageRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.payslipRepository = payslipRepository;
        this.payrollPeriodRepository = payrollPeriodRepository;
        this.contractLegalValidator = contractLegalValidator;
        this.payrollEngine = payrollEngine;
        this.payrollContextBuilder = payrollContextBuilder;
        this.objectMapper = objectMapper;
    }

    public void validateAndEnrichEmployee(Employee employee, UUID excludeId) {
        if (employee.getFirstName() == null || employee.getFirstName().isBlank()) {
            throw new IllegalArgumentException("Employee first name is required");
        }
        if (employee.getLastName() == null || employee.getLastName().isBlank()) {
            throw new IllegalArgumentException("Employee last name is required");
        }

        employee.setFirstName(employee.getFirstName().trim());
        employee.setLastName(employee.getLastName().trim());
        if (employee.getCin() != null) {
            employee.setCin(employee.getCin().trim());
        }
        if (employee.getCnssMatricule() != null) {
            employee.setCnssMatricule(employee.getCnssMatricule().trim());
        }
        if (employee.getEmail() != null) {
            employee.setEmail(employee.getEmail().trim());
        }

        if (excludeId == null
                && (employee.getEmployeeNumber() == null || employee.getEmployeeNumber().isBlank())) {
            String suffix = String.valueOf(System.currentTimeMillis());
            if (suffix.length() > 8) {
                suffix = suffix.substring(suffix.length() - 8);
            }
            employee.setEmployeeNumber("EMP-" + suffix);
        }

        if (employee.getStatus() == null) {
            employee.setStatus(EmployeeStatus.ACTIVE);
        }

        if (employee.getBirthDate() != null
                && employee.getHireDate() != null
                && employee.getBirthDate().isAfter(employee.getHireDate())) {
            throw new IllegalArgumentException("Birth date cannot be after hire date");
        }

        if (employee.getCin() != null && !employee.getCin().isBlank()
                && employeeRepository.existsByCinAndIdNotAndIsDeletedFalse(employee.getCin(), excludeId)) {
            throw new IllegalArgumentException("CIN already assigned to another employee");
        }

        if (employee.getCnssMatricule() != null && !employee.getCnssMatricule().isBlank()
                && employeeRepository.existsByCnssMatriculeAndIdNotAndIsDeletedFalse(
                employee.getCnssMatricule(), excludeId)) {
            throw new IllegalArgumentException("CNSS matricule already assigned to another employee");
        }

        if (employee.getStatus() == EmployeeStatus.TERMINATED && employee.getId() != null) {
            terminateActiveContracts(employee.getId());
        }
    }

    @Transactional
    public void terminateActiveContracts(UUID employeeId) {
        List<EmploymentContract> activeContracts = contractRepository
                .findByEmployee_IdAndStatusAndIsDeletedFalse(employeeId, ContractStatus.ACTIVE);
        for (EmploymentContract contract : activeContracts) {
            contract.setStatus(ContractStatus.TERMINATED);
            contractRepository.save(contract);
        }
    }

    public void validateAndEnrichContract(EmploymentContract contract, UUID excludeId) {
        if (contract.getEmployee() == null) {
            throw new IllegalArgumentException("Employee is required on employment contract");
        }
        if (contract.getPoste() == null) {
            throw new IllegalArgumentException("Poste is required on employment contract");
        }
        if (contract.getStartDate() == null) {
            throw new IllegalArgumentException("Contract start date is required");
        }
        if (contract.getContractType() == ContractType.CDI) {
            contract.setEndDate(null);
        }

        syncSalaryFields(contract);

        List<String> legalViolations = contractLegalValidator.validate(contract);
        if (!legalViolations.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", legalViolations));
        }

        Employee employee = contract.getEmployee();
        if (employee.getHireDate() != null && contract.getStartDate().isBefore(employee.getHireDate())) {
            throw new IllegalArgumentException("Contract cannot start before employee hire date");
        }

        if (contract.getStatus() == null) {
            contract.setStatus(ContractStatus.DRAFT);
        }

        if (contract.getStatus() == ContractStatus.ACTIVE) {
            UUID employeeId = employee.getId();
            List<EmploymentContract> activeContracts = contractRepository
                    .findByEmployee_IdAndStatusAndIsDeletedFalse(employeeId, ContractStatus.ACTIVE);
            for (EmploymentContract other : activeContracts) {
                if (excludeId == null || !other.getId().equals(excludeId)) {
                    other.setStatus(ContractStatus.EXPIRED);
                    contractRepository.save(other);
                }
            }
        }
    }

    private void syncSalaryFields(EmploymentContract contract) {
        if (contract.getBaseSalary() == null && contract.getSalary() != null) {
            contract.setBaseSalary(BigDecimal.valueOf(contract.getSalary()));
        }
        if (contract.getSalary() == null && contract.getBaseSalary() != null) {
            contract.setSalary(contract.getBaseSalary().doubleValue());
        }
    }

    @Transactional
    public void syncEmployeeFromActiveContract(EmploymentContract contract) {
        if (contract.getStatus() != ContractStatus.ACTIVE || contract.getEmployee() == null) {
            return;
        }
        Employee employee = employeeRepository.findByIdAndIsDeletedFalse(contract.getEmployee().getId())
                .orElse(null);
        if (employee == null) {
            return;
        }
        Poste poste = contract.getPoste();
        if (poste != null && poste.getTitle() != null && !poste.getTitle().isBlank()) {
            employee.setJobTitle(poste.getTitle());
        }
        employeeRepository.save(employee);
    }

    public Optional<EmploymentContract> findActiveContractOnDate(UUID employeeId, LocalDate date) {
        if (employeeId == null || date == null) {
            return Optional.empty();
        }
        return contractRepository.findActiveForEmployeeOnDate(employeeId, date);
    }

    public void validateAndEnrichPointage(Pointage pointage, UUID excludeId) {
        if (pointage.getEmployee() == null) {
            throw new IllegalArgumentException("Employee is required on pointage");
        }
        if (pointage.getWorkDate() == null) {
            throw new IllegalArgumentException("Work date is required on pointage");
        }
        if (pointage.getWorkDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Work date cannot be in the future");
        }

        UUID employeeId = pointage.getEmployee().getId();
        if (pointageRepository.existsByEmployee_IdAndWorkDateAndIdNotAndIsDeletedFalse(
                employeeId, pointage.getWorkDate(), excludeId)) {
            throw new IllegalArgumentException("Pointage already exists for this employee on " + pointage.getWorkDate());
        }

        findActiveContractOnDate(employeeId, pointage.getWorkDate())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No active employment contract covers work date " + pointage.getWorkDate()));

        enrichWorkedHours(pointage);

        if (pointage.getStatus() == null) {
            if (leaveRequestRepository.hasApprovedLeaveOn(employeeId, pointage.getWorkDate())) {
                pointage.setStatus(AttendanceStatus.LEAVE);
            } else {
                pointage.setStatus(AttendanceStatus.PRESENT);
            }
        } else if (pointage.getStatus() == AttendanceStatus.PRESENT
                && leaveRequestRepository.hasApprovedLeaveOn(employeeId, pointage.getWorkDate())) {
            pointage.setStatus(AttendanceStatus.LEAVE);
        }
    }

    public void validateAndEnrichLeave(LeaveRequest leave, UUID excludeId) {
        if (leave.getEmployee() == null) {
            throw new IllegalArgumentException("Employee is required on leave request");
        }
        if (leave.getStartDate() == null || leave.getEndDate() == null) {
            throw new IllegalArgumentException("Leave start and end dates are required");
        }
        if (leave.getEndDate().isBefore(leave.getStartDate())) {
            throw new IllegalArgumentException("Leave end date cannot be before start date");
        }

        UUID employeeId = leave.getEmployee().getId();
        findActiveContractOnDate(employeeId, leave.getStartDate())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No active employment contract covers leave start date"));

        if (leaveRequestRepository.existsOverlapping(
                employeeId, leave.getStartDate(), leave.getEndDate(), excludeId)) {
            throw new IllegalArgumentException("Leave request overlaps an existing pending or approved leave");
        }

        leave.setDurationDays((double) (ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1));

        if (leave.getStatus() == null) {
            leave.setStatus(LeaveStatus.PENDING);
        }
    }

    public void validateAndEnrichPayslip(Payslip payslip, UUID excludeId) {
        if (payslip.getEmployee() == null) {
            throw new IllegalArgumentException("Employee is required on payslip");
        }
        if (payslip.getPayrollPeriod() == null) {
            throw new IllegalArgumentException("Payroll period is required on payslip");
        }

        PayrollPeriod period = payslip.getPayrollPeriod();
        if (period.getStatus() != null && LOCKED_PAYROLL_STATUSES.contains(period.getStatus())) {
            throw new IllegalArgumentException("Payroll period is closed; payslips cannot be modified");
        }

        UUID employeeId = payslip.getEmployee().getId();
        UUID periodId = period.getId();

        if (payslipRepository.existsByEmployee_IdAndPayrollPeriod_IdAndIdNotAndIsDeletedFalse(
                employeeId, periodId, excludeId)) {
            throw new IllegalArgumentException("A payslip already exists for this employee in the selected payroll period");
        }

        LocalDate periodStart = period.getPeriodStart();
        LocalDate periodEnd = period.getPeriodEnd();
        if (periodStart != null && periodEnd != null) {
            findActiveContractOnDate(employeeId, periodStart)
                    .or(() -> findActiveContractOnDate(employeeId, periodEnd))
                    .ifPresent(contract -> prefillPayslipFromContract(payslip, contract));
        }

        enrichPayslipAmounts(payslip);

        if (Boolean.TRUE.equals(payslip.getPaid())) {
            payslip.setStatus(PayslipStatus.PAID);
            if (payslip.getPaymentDate() == null) {
                payslip.setPaymentDate(LocalDate.now());
            }
        } else if (payslip.getStatus() == null) {
            payslip.setStatus(PayslipStatus.DRAFT);
        }
    }

    public void validateAndEnrichPayrollPeriod(PayrollPeriod period, UUID excludeId) {
        if (period.getYear() == null || period.getMonth() == null) {
            throw new IllegalArgumentException("Payroll period year and month are required");
        }
        if (period.getMonth() < 1 || period.getMonth() > 12) {
            throw new IllegalArgumentException("Payroll month must be between 1 and 12");
        }
        if (period.getPeriodStart() == null || period.getPeriodEnd() == null) {
            throw new IllegalArgumentException("Payroll period start and end dates are required");
        }
        if (period.getPeriodEnd().isBefore(period.getPeriodStart())) {
            throw new IllegalArgumentException("Payroll period end cannot be before start");
        }

        if (payrollPeriodRepository.existsByYearAndMonthAndIdNotAndIsDeletedFalse(
                period.getYear(), period.getMonth(), excludeId)) {
            throw new IllegalArgumentException(
                    "A payroll period already exists for " + period.getYear() + "-" + period.getMonth());
        }

        if (period.getStatus() == null) {
            period.setStatus(PayrollPeriodStatus.OPEN);
        }
    }

    public void validatePayrollPeriodStatusTransition(PayrollPeriodStatus current, PayrollPeriodStatus next) {
        if (current == null || next == null || current == next) {
            return;
        }
        boolean valid = switch (current) {
            case OPEN -> next == PayrollPeriodStatus.CALCULATED || next == PayrollPeriodStatus.CLOSED;
            case CALCULATED -> next == PayrollPeriodStatus.VALIDATED || next == PayrollPeriodStatus.OPEN;
            case VALIDATED -> next == PayrollPeriodStatus.PAID || next == PayrollPeriodStatus.CALCULATED;
            case PAID -> next == PayrollPeriodStatus.CLOSED;
            case CLOSED -> false;
        };
        if (!valid) {
            throw new IllegalArgumentException(
                    "Invalid payroll period status transition from " + current + " to " + next);
        }
    }

    public void assertLeavePending(LeaveRequest leave) {
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalArgumentException("Only pending leave requests can be approved or rejected");
        }
    }

    private void prefillPayslipFromContract(Payslip payslip, EmploymentContract contract) {
        Double salary = resolveContractSalaryAsDouble(contract);
        if (salary == null) {
            return;
        }
        if (payslip.getBaseSalary() == null) {
            payslip.setBaseSalary(salary);
        }
        if (payslip.getGrossSalary() == null) {
            double bonuses = payslip.getBonuses() != null ? payslip.getBonuses() : 0d;
            payslip.setGrossSalary(payslip.getBaseSalary() + bonuses);
        }
    }

    private Double resolveContractSalaryAsDouble(EmploymentContract contract) {
        if (contract.getBaseSalary() != null) {
            return contract.getBaseSalary().doubleValue();
        }
        return contract.getSalary();
    }

    private void enrichPayslipAmounts(Payslip payslip) {
        PayrollInputs inputs = payrollContextBuilder.fromPayslip(payslip);
        BigDecimal base = payslip.getBaseSalary() != null
                ? BigDecimal.valueOf(payslip.getBaseSalary())
                : BigDecimal.ZERO;

        LocalDate asOf = payrollContextBuilder.resolveAsOf(payslip.getPayrollPeriod());
        WeeklyRegimeType regime = payrollContextBuilder.resolveWeeklyRegime(
                payslip.getEmployee(),
                WeeklyRegimeType.HOURS_48
        );

        PayrollResult result = payrollEngine.calculate(base, inputs, asOf, regime);

        payslip.setBaseSalary(toDouble(result.getBaseSalary()));
        payslip.setGrossSalary(toDouble(result.getGrossSalary()));
        payslip.setBonuses(toDouble(inputs.getBonuses()));
        payslip.setCnssEmployee(toDouble(result.getEmployeeCnss()));
        payslip.setCnssEmployer(toDouble(result.getEmployerCnss()));
        payslip.setCss(toDouble(result.getCss()));
        payslip.setIrpp(toDouble(result.getIncomeTax()));
        payslip.setNetSalary(toDouble(result.getNetSalary()));
        payslip.setTaxableSalary(toDouble(result.getTaxableSalary()));
        payslip.setCnssBase(toDouble(result.getCnssBase()));
        payslip.setEmployerCost(toDouble(result.getEmployerCost()));
        payslip.setOtherDeductions(toDouble(result.getOtherDeductions()));
        payslip.setCalculationSnapshot(result.getLegalSnapshotJson());
        payslip.setCalculationBreakdown(toBreakdownJson(result));
    }

    private String toBreakdownJson(PayrollResult result) {
        try {
            return objectMapper.writeValueAsString(result.getBreakdown());
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private static Double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : null;
    }

    private void enrichWorkedHours(Pointage pointage) {
        if (pointage.getWorkedHours() != null) {
            return;
        }
        if (pointage.getCheckIn() == null || pointage.getCheckOut() == null) {
            return;
        }
        long totalMinutes = Duration.between(pointage.getCheckIn(), pointage.getCheckOut()).toMinutes();
        int breakMinutes = pointage.getBreakMinutes() != null ? pointage.getBreakMinutes() : 0;
        long workedMinutes = Math.max(0, totalMinutes - breakMinutes);
        pointage.setWorkedHours(round2(workedMinutes / 60.0));
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
