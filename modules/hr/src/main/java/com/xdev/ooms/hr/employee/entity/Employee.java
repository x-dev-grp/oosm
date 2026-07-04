package com.xdev.ooms.hr.employee.entity;

import com.xdev.ooms.hr.common.enums.EmployeeStatus;
import com.xdev.ooms.hr.common.enums.PaymentMode;
import com.xdev.ooms.hr.common.enums.SalaryType;
import com.xdev.ooms.hr.common.enums.WorkRegime;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "hr_employee")
public class Employee extends BaseEntity implements Serializable {

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(length = 32)
    private String cin;

    @Column(length = 32)
    private String cnssMatricule;

    private String email;
    private String phone;
    private String address;

    private LocalDate birthDate;
    private LocalDate hireDate;

    private String jobTitle;
    private String department;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    @Enumerated(EnumType.STRING)
    private SalaryType salaryType;

    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;

    private String bankAccountRef;

    @Enumerated(EnumType.STRING)
    private WorkRegime workRegime;

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
}
