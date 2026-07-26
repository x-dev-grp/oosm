package com.xdev.ooms.hr.employee.entity;

import com.xdev.ooms.hr.common.enums.EmployeeStatus;
import com.xdev.ooms.hr.common.enums.Gender;
import com.xdev.ooms.hr.common.enums.MaritalStatus;
import com.xdev.ooms.hr.common.enums.PaymentMode;
import com.xdev.ooms.hr.common.enums.SalaryType;
import com.xdev.ooms.hr.common.enums.WorkRegime;
import com.xdev.ooms.hr.organization.entity.Department;
import com.xdev.ooms.hr.organization.entity.EmployeeCategory;
import com.xdev.ooms.hr.organization.entity.Grade;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    private String employeeNumber;

    @Column(length = 32)
    private String cin;

    @Column(length = 32)
    private String cnssMatricule;

    private String email;
    private String phone;
    private String address;

    private LocalDate birthDate;
    private LocalDate hireDate;
    private LocalDate terminationDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;

    private Integer numberOfChildren;

    private String taxIdentifier;
    private String rib;

    private String jobTitle;
    private String department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id")
    private Grade grade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_category_id")
    private EmployeeCategory employeeCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_ref_id")
    private Department departmentRef;

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

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
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

    public LocalDate getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(LocalDate terminationDate) {
        this.terminationDate = terminationDate;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public Integer getNumberOfChildren() {
        return numberOfChildren;
    }

    public void setNumberOfChildren(Integer numberOfChildren) {
        this.numberOfChildren = numberOfChildren;
    }

    public String getTaxIdentifier() {
        return taxIdentifier;
    }

    public void setTaxIdentifier(String taxIdentifier) {
        this.taxIdentifier = taxIdentifier;
    }

    public String getRib() {
        return rib;
    }

    public void setRib(String rib) {
        this.rib = rib;
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

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    public EmployeeCategory getEmployeeCategory() {
        return employeeCategory;
    }

    public void setEmployeeCategory(EmployeeCategory employeeCategory) {
        this.employeeCategory = employeeCategory;
    }

    public Department getDepartmentRef() {
        return departmentRef;
    }

    public void setDepartmentRef(Department departmentRef) {
        this.departmentRef = departmentRef;
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
