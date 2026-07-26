package com.xdev.ooms.sharedkernel.ports;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Cross-module command to post a validated payroll period into finance.
 * Idempotency key should be stable (e.g. payroll period id).
 */
public record PayrollAccountingCommand(
        UUID tenantId,
        UUID payrollPeriodId,
        String periodLabel,
        BigDecimal gross,
        BigDecimal deductions,
        BigDecimal net,
        BigDecimal employerContributions,
        BigDecimal totalEmployerCost,
        String idempotencyKey
) {
}
