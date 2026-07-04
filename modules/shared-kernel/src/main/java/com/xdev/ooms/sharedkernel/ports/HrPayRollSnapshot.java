package com.xdev.ooms.sharedkernel.ports;

import java.time.LocalDate;
import java.util.UUID;

public record HrPayRollSnapshot(
        UUID payrollId,
        LocalDate periodStart,
        LocalDate periodEnd,
        Double grossSalary,
        Double baseSalary,
        Double bonuses,
        Double cnssEmployee,
        Double cnssEmployer,
        Double irpp,
        Double css,
        Double netSalary,
        Boolean paid,
        LocalDate paymentDate,
        String employeeFirstName,
        String employeeLastName,
        String employeeCin,
        String employeeCnssMatricule) {
}
