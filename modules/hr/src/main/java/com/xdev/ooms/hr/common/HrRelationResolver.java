package com.xdev.ooms.hr.common;

import com.xdev.ooms.hr.employee.entity.Employee;
import com.xdev.ooms.hr.employee.repository.EmployeeRepository;
import com.xdev.ooms.hr.payroll.entity.PayrollPeriod;
import com.xdev.ooms.hr.payroll.repository.PayrollPeriodRepository;
import com.xdev.ooms.hr.poste.entity.Poste;
import com.xdev.ooms.hr.poste.repository.PosteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class HrRelationResolver {

    private final EmployeeRepository employeeRepository;
    private final PosteRepository posteRepository;
    private final PayrollPeriodRepository payrollPeriodRepository;

    public HrRelationResolver(
            EmployeeRepository employeeRepository,
            PosteRepository posteRepository,
            PayrollPeriodRepository payrollPeriodRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.posteRepository = posteRepository;
        this.payrollPeriodRepository = payrollPeriodRepository;
    }

    public Employee resolveEmployee(Employee employee) {
        if (employee == null || employee.getId() == null) {
            return null;
        }
        return employeeRepository.findByIdAndIsDeletedFalse(employee.getId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employee.getId()));
    }

    public Poste resolvePoste(Poste poste) {
        if (poste == null || poste.getId() == null) {
            return null;
        }
        return posteRepository.findByIdAndIsDeletedFalse(poste.getId())
                .orElseThrow(() -> new EntityNotFoundException("Poste not found with id: " + poste.getId()));
    }

    public PayrollPeriod resolvePayrollPeriod(PayrollPeriod payrollPeriod) {
        if (payrollPeriod == null || payrollPeriod.getId() == null) {
            return null;
        }
        return payrollPeriodRepository.findByIdAndIsDeletedFalse(payrollPeriod.getId())
                .orElseThrow(() -> new EntityNotFoundException("Payroll period not found with id: " + payrollPeriod.getId()));
    }
}
