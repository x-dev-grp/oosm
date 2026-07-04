package com.xdev.ooms.hr.employee.dto;

import com.xdev.ooms.hr.common.enums.EmployeeStatus;
import com.xdev.ooms.hr.common.enums.PaymentMode;
import com.xdev.ooms.hr.common.enums.SalaryType;
import com.xdev.ooms.hr.common.enums.WorkRegime;
import com.xdev.ooms.hr.contract.dto.EmploymentContractDto;
import com.xdev.ooms.hr.employee.entity.Employee;
import com.xdev.ooms.hr.leave.dto.LeaveRequestDto;
import com.xdev.ooms.hr.payslip.dto.PayslipDto;
import com.xdev.ooms.hr.pointage.dto.PointageDto;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.time.LocalDate;
import java.util.List;

public class EmployeeDto extends BaseDto<Employee> {
    private String firstName;
    private String lastName;
    private String cin;
    private String cnssMatricule;
    private String email;
    private String phone;
    private String address;
    private LocalDate birthDate;
    private LocalDate hireDate;
    private String jobTitle;
    private String department;
    private EmployeeStatus status;
    private SalaryType salaryType;
    private PaymentMode paymentMode;
    private String bankAccountRef;
    private WorkRegime workRegime;
    private List<EmploymentContractDto> contracts;
    private EmploymentContractDto activeContract;
    private List<PointageDto> pointages;
    private List<LeaveRequestDto> leaveRequests;
    private List<PayslipDto> payslips;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getCnssMatricule() {
        return cnssMatricule;
    }

    public void setCnssMatricule(String cnssMatricule) {
        this.cnssMatricule = cnssMatricule;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public EmployeeStatus getStatus() {
        return status;
    }

    public void setStatus(EmployeeStatus status) {
        this.status = status;
    }

    public SalaryType getSalaryType() {
        return salaryType;
    }

    public void setSalaryType(SalaryType salaryType) {
        this.salaryType = salaryType;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentMode paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getBankAccountRef() {
        return bankAccountRef;
    }

    public void setBankAccountRef(String bankAccountRef) {
        this.bankAccountRef = bankAccountRef;
    }

    public WorkRegime getWorkRegime() {
        return workRegime;
    }

    public void setWorkRegime(WorkRegime workRegime) {
        this.workRegime = workRegime;
    }

    public List<EmploymentContractDto> getContracts() {
        return contracts;
    }

    public void setContracts(List<EmploymentContractDto> contracts) {
        this.contracts = contracts;
    }

    public EmploymentContractDto getActiveContract() {
        return activeContract;
    }

    public void setActiveContract(EmploymentContractDto activeContract) {
        this.activeContract = activeContract;
    }

    public List<PointageDto> getPointages() {
        return pointages;
    }

    public void setPointages(List<PointageDto> pointages) {
        this.pointages = pointages;
    }

    public List<LeaveRequestDto> getLeaveRequests() {
        return leaveRequests;
    }

    public void setLeaveRequests(List<LeaveRequestDto> leaveRequests) {
        this.leaveRequests = leaveRequests;
    }

    public List<PayslipDto> getPayslips() {
        return payslips;
    }

    public void setPayslips(List<PayslipDto> payslips) {
        this.payslips = payslips;
    }
}
