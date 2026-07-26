package com.xdev.ooms.hr.common;

import com.xdev.ooms.hr.contract.entity.EmploymentContract;
import com.xdev.ooms.hr.contract.repository.EmploymentContractRepository;
import com.xdev.ooms.hr.employee.entity.Employee;
import com.xdev.ooms.hr.employee.repository.EmployeeRepository;
import com.xdev.ooms.hr.loan.entity.EmployeeLoan;
import com.xdev.ooms.hr.loan.repository.EmployeeLoanRepository;
import com.xdev.ooms.hr.organization.entity.Department;
import com.xdev.ooms.hr.organization.entity.EmployeeCategory;
import com.xdev.ooms.hr.organization.entity.Grade;
import com.xdev.ooms.hr.organization.repository.DepartmentRepository;
import com.xdev.ooms.hr.organization.repository.EmployeeCategoryRepository;
import com.xdev.ooms.hr.organization.repository.GradeRepository;
import com.xdev.ooms.hr.payroll.entity.PayrollPeriod;
import com.xdev.ooms.hr.payroll.repository.PayrollPeriodRepository;
import com.xdev.ooms.hr.pointage.entity.Pointage;
import com.xdev.ooms.hr.pointage.repository.PointageRepository;
import com.xdev.ooms.hr.poste.entity.Poste;
import com.xdev.ooms.hr.poste.repository.PosteRepository;
import com.xdev.ooms.hr.timesheet.entity.Timesheet;
import com.xdev.ooms.hr.timesheet.repository.TimesheetRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class HrRelationResolver {

    private final EmployeeRepository employeeRepository;
    private final PosteRepository posteRepository;
    private final PayrollPeriodRepository payrollPeriodRepository;
    private final DepartmentRepository departmentRepository;
    private final GradeRepository gradeRepository;
    private final EmployeeCategoryRepository employeeCategoryRepository;
    private final EmploymentContractRepository employmentContractRepository;
    private final PointageRepository pointageRepository;
    private final TimesheetRepository timesheetRepository;
    private final EmployeeLoanRepository employeeLoanRepository;

    public HrRelationResolver(
            EmployeeRepository employeeRepository,
            PosteRepository posteRepository,
            PayrollPeriodRepository payrollPeriodRepository,
            DepartmentRepository departmentRepository,
            GradeRepository gradeRepository,
            EmployeeCategoryRepository employeeCategoryRepository,
            EmploymentContractRepository employmentContractRepository,
            PointageRepository pointageRepository,
            TimesheetRepository timesheetRepository,
            EmployeeLoanRepository employeeLoanRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.posteRepository = posteRepository;
        this.payrollPeriodRepository = payrollPeriodRepository;
        this.departmentRepository = departmentRepository;
        this.gradeRepository = gradeRepository;
        this.employeeCategoryRepository = employeeCategoryRepository;
        this.employmentContractRepository = employmentContractRepository;
        this.pointageRepository = pointageRepository;
        this.timesheetRepository = timesheetRepository;
        this.employeeLoanRepository = employeeLoanRepository;
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

    public Department resolveDepartment(Department department) {
        if (department == null || department.getId() == null) {
            return null;
        }
        return departmentRepository.findByIdAndIsDeletedFalse(department.getId())
                .orElseThrow(() -> new EntityNotFoundException("Department not found with id: " + department.getId()));
    }

    public Grade resolveGrade(Grade grade) {
        if (grade == null || grade.getId() == null) {
            return null;
        }
        return gradeRepository.findByIdAndIsDeletedFalse(grade.getId())
                .orElseThrow(() -> new EntityNotFoundException("Grade not found with id: " + grade.getId()));
    }

    public EmployeeCategory resolveEmployeeCategory(EmployeeCategory category) {
        if (category == null || category.getId() == null) {
            return null;
        }
        return employeeCategoryRepository.findByIdAndIsDeletedFalse(category.getId())
                .orElseThrow(() -> new EntityNotFoundException("Employee category not found with id: " + category.getId()));
    }

    public EmploymentContract resolveEmploymentContract(EmploymentContract contract) {
        if (contract == null || contract.getId() == null) {
            return null;
        }
        return employmentContractRepository.findByIdAndIsDeletedFalse(contract.getId())
                .orElseThrow(() -> new EntityNotFoundException("Employment contract not found with id: " + contract.getId()));
    }

    public Pointage resolvePointage(Pointage pointage) {
        if (pointage == null || pointage.getId() == null) {
            return null;
        }
        return pointageRepository.findByIdAndIsDeletedFalse(pointage.getId())
                .orElseThrow(() -> new EntityNotFoundException("Pointage not found with id: " + pointage.getId()));
    }

    public Timesheet resolveTimesheet(Timesheet timesheet) {
        if (timesheet == null || timesheet.getId() == null) {
            return null;
        }
        return timesheetRepository.findByIdAndIsDeletedFalse(timesheet.getId())
                .orElseThrow(() -> new EntityNotFoundException("Timesheet not found with id: " + timesheet.getId()));
    }

    public EmployeeLoan resolveEmployeeLoan(EmployeeLoan loan) {
        if (loan == null || loan.getId() == null) {
            return null;
        }
        return employeeLoanRepository.findByIdAndIsDeletedFalse(loan.getId())
                .orElseThrow(() -> new EntityNotFoundException("Employee loan not found with id: " + loan.getId()));
    }
}
