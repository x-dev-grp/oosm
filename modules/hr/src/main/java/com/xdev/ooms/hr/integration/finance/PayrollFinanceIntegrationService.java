package com.xdev.ooms.hr.integration.finance;

import com.xdev.ooms.hr.payroll.entity.PayrollPeriod;
import com.xdev.ooms.hr.payroll.repository.PayrollPeriodRepository;
import com.xdev.ooms.hr.payslip.entity.Payslip;
import com.xdev.ooms.hr.payslip.repository.PayslipRepository;
import com.xdev.ooms.sharedkernel.ports.PayrollAccountingCommand;
import com.xdev.ooms.sharedkernel.ports.PayrollAccountingPort;
import com.xdev.ooms.sharedkernel.utils.AuditHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Posts validated payroll periods to finance via {@link PayrollAccountingPort}.
 * Idempotent using {@link PayrollPeriod#getFinancePosted()}.
 */
@Service
public class PayrollFinanceIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(PayrollFinanceIntegrationService.class);

    private final PayrollAccountingPort payrollAccountingPort;
    private final PayslipRepository payslipRepository;
    private final PayrollPeriodRepository payrollPeriodRepository;

    public PayrollFinanceIntegrationService(
            PayrollAccountingPort payrollAccountingPort,
            PayslipRepository payslipRepository,
            PayrollPeriodRepository payrollPeriodRepository
    ) {
        this.payrollAccountingPort = payrollAccountingPort;
        this.payslipRepository = payslipRepository;
        this.payrollPeriodRepository = payrollPeriodRepository;
    }

    @Transactional
    public void onPayrollValidated(PayrollPeriod period) {
        if (period == null || period.getId() == null) {
            return;
        }
        if (Boolean.TRUE.equals(period.getFinancePosted())) {
            log.info("Payroll period {} already posted to finance (ref={})", period.getId(), period.getFinanceReference());
            return;
        }

        List<Payslip> payslips = payslipRepository.findByPayrollPeriod_IdAndIsDeletedFalse(period.getId());
        PayrollValidatedEvent event = buildEvent(period, payslips);

        String idempotencyKey = "PAYROLL-" + period.getId();
        try {
            String financeRef = payrollAccountingPort.postPayroll(new PayrollAccountingCommand(
                    event.tenantId(),
                    event.payrollPeriodId(),
                    event.periodLabel(),
                    event.gross(),
                    event.deductions(),
                    event.net(),
                    event.employerContributions(),
                    event.totalEmployerCost(),
                    idempotencyKey
            ));
            period.setFinancePosted(Boolean.TRUE);
            period.setFinanceReference(financeRef);
            AuditHelper.applyAuditOnCreate(period);
            payrollPeriodRepository.save(period);
            log.info("Posted payroll period {} to finance, ref={}", period.getId(), financeRef);
        } catch (RuntimeException ex) {
            log.error("Failed to post payroll period {} to finance: {}", period.getId(), ex.getMessage(), ex);
            throw ex;
        }
    }

    private PayrollValidatedEvent buildEvent(PayrollPeriod period, List<Payslip> payslips) {
        BigDecimal gross = BigDecimal.ZERO;
        BigDecimal net = BigDecimal.ZERO;
        BigDecimal employerCnss = BigDecimal.ZERO;
        BigDecimal employerCost = BigDecimal.ZERO;
        BigDecimal employeeDeductions = BigDecimal.ZERO;

        for (Payslip p : payslips) {
            gross = gross.add(money(p.getGrossSalary()));
            net = net.add(money(p.getNetSalary()));
            employerCnss = employerCnss.add(money(p.getCnssEmployer()));
            employerCost = employerCost.add(money(p.getEmployerCost()));
            employeeDeductions = employeeDeductions
                    .add(money(p.getCnssEmployee()))
                    .add(money(p.getIrpp()))
                    .add(money(p.getCss()))
                    .add(money(p.getOtherDeductions()));
        }

        if (employerCost.compareTo(BigDecimal.ZERO) == 0 && gross.compareTo(BigDecimal.ZERO) > 0) {
            employerCost = gross.add(employerCnss);
        }

        String label = period.getYear() + "-" + String.format("%02d", period.getMonth() != null ? period.getMonth() : 0);
        return new PayrollValidatedEvent(
                period.getTenantId(),
                period.getId(),
                label,
                scale(gross),
                scale(employeeDeductions),
                scale(net),
                scale(employerCnss),
                scale(employerCost)
        );
    }

    private static BigDecimal money(Double value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(value);
    }

    private static BigDecimal scale(BigDecimal value) {
        return value.setScale(4, RoundingMode.HALF_UP);
    }
}
