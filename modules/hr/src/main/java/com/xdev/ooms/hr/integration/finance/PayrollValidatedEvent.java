package com.xdev.ooms.hr.integration.finance;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Domain event raised when a payroll period reaches VALIDATED (finance posting trigger).
 */
public record PayrollValidatedEvent(
        UUID tenantId,
        UUID payrollPeriodId,
        String periodLabel,
        BigDecimal gross,
        BigDecimal deductions,
        BigDecimal net,
        BigDecimal employerContributions,
        BigDecimal totalEmployerCost
) {
}
