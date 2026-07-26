package com.xdev.ooms.hr.dashboard.service;

import com.xdev.ooms.hr.common.enums.EmployeeStatus;
import com.xdev.ooms.hr.common.enums.LeaveStatus;
import com.xdev.ooms.hr.compliance.dto.ComplianceSummaryDto;
import com.xdev.ooms.hr.compliance.service.ComplianceEngine;
import com.xdev.ooms.hr.contract.repository.EmploymentContractRepository;
import com.xdev.ooms.hr.dashboard.dto.HrDashboardStatsDto;
import com.xdev.ooms.hr.employee.repository.EmployeeRepository;
import com.xdev.ooms.hr.leave.repository.LeaveRequestRepository;
import com.xdev.ooms.hr.payroll.entity.PayrollPeriod;
import com.xdev.ooms.hr.payroll.repository.PayrollPeriodRepository;
import com.xdev.ooms.hr.pointage.repository.PointageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class HrDashboardService {

    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmploymentContractRepository contractRepository;
    private final PointageRepository pointageRepository;
    private final PayrollPeriodRepository payrollPeriodRepository;
    private final ComplianceEngine complianceEngine;

    public HrDashboardService(
            EmployeeRepository employeeRepository,
            LeaveRequestRepository leaveRequestRepository,
            EmploymentContractRepository contractRepository,
            PointageRepository pointageRepository,
            PayrollPeriodRepository payrollPeriodRepository,
            ComplianceEngine complianceEngine
    ) {
        this.employeeRepository = employeeRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.contractRepository = contractRepository;
        this.pointageRepository = pointageRepository;
        this.payrollPeriodRepository = payrollPeriodRepository;
        this.complianceEngine = complianceEngine;
    }

    @Transactional(readOnly = true)
    public HrDashboardStatsDto getStats() {
        LocalDate today = LocalDate.now();
        HrDashboardStatsDto stats = new HrDashboardStatsDto();

        long total = employeeRepository.countByIsDeletedFalse();
        long active = employeeRepository.countByStatusAndIsDeletedFalse(EmployeeStatus.ACTIVE);
        long presentToday = pointageRepository.countPresentOn(today);
        long onLeave = leaveRequestRepository.countEmployeesOnLeaveOn(today);
        long pendingLeave = leaveRequestRepository.countByStatusAndIsDeletedFalse(LeaveStatus.PENDING);
        long expiring = contractRepository.countExpiringBetween(today, today.plusDays(30));
        long anomalies = pointageRepository.countRecentAnomalies(today.minusDays(7));

        long absentToday = Math.max(0, active - presentToday - onLeave);

        YearMonth ym = YearMonth.from(today);
        String payrollStatus = payrollPeriodRepository
                .findByYearAndMonthAndIsDeletedFalse(ym.getYear(), ym.getMonthValue())
                .map(PayrollPeriod::getStatus)
                .map(Enum::name)
                .orElse("NONE");

        ComplianceSummaryDto compliance = complianceEngine.getSummary();

        stats.setTotalEmployees(total);
        stats.setActiveEmployees(active);
        stats.setPresentToday(presentToday);
        stats.setAbsentToday(absentToday);
        stats.setOnLeave(onLeave);
        stats.setPendingLeaveRequests(pendingLeave);
        stats.setContractsExpiringSoon(expiring);
        stats.setAttendanceAnomalies(anomalies);
        stats.setPayrollStatus(payrollStatus);
        stats.setComplianceScore(compliance.getScore());
        stats.setCriticalComplianceIssues(compliance.getCriticalCount());
        return stats;
    }
}
