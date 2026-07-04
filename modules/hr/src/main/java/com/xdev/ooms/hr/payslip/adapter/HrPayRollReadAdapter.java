package com.xdev.ooms.hr.payslip.adapter;

import com.xdev.ooms.hr.employee.entity.Employee;
import com.xdev.ooms.hr.payroll.entity.PayrollPeriod;
import com.xdev.ooms.hr.payslip.entity.Payslip;
import com.xdev.ooms.hr.payslip.repository.PayslipRepository;
import com.xdev.ooms.sharedkernel.ports.HrPayRollReadPort;
import com.xdev.ooms.sharedkernel.ports.HrPayRollSnapshot;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class HrPayRollReadAdapter implements HrPayRollReadPort {

    private final PayslipRepository payslipRepository;

    public HrPayRollReadAdapter(PayslipRepository payslipRepository) {
        this.payslipRepository = payslipRepository;
    }

    @Override
    public Optional<HrPayRollSnapshot> findPayRollForPdf(UUID payrollId) {
        return payslipRepository.findWithDetailsByIdAndIsDeletedFalse(payrollId).map(this::toSnapshot);
    }

    private HrPayRollSnapshot toSnapshot(Payslip payslip) {
        Employee employee = payslip.getEmployee();
        PayrollPeriod period = payslip.getPayrollPeriod();
        return new HrPayRollSnapshot(
                payslip.getId(),
                period != null ? period.getPeriodStart() : null,
                period != null ? period.getPeriodEnd() : null,
                payslip.getGrossSalary(),
                payslip.getBaseSalary(),
                payslip.getBonuses(),
                payslip.getCnssEmployee(),
                payslip.getCnssEmployer(),
                payslip.getIrpp(),
                payslip.getCss(),
                payslip.getNetSalary(),
                payslip.getPaid(),
                payslip.getPaymentDate(),
                employee != null ? employee.getFirstName() : null,
                employee != null ? employee.getLastName() : null,
                employee != null ? employee.getCin() : null,
                employee != null ? employee.getCnssMatricule() : null
        );
    }
}
