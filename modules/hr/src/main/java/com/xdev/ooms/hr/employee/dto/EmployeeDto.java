package com.xdev.ooms.hr.employee.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.xdev.ooms.hr.contract.dto.ContractDto;
import com.xdev.ooms.hr.department.dto.DepartmentDto;
import com.xdev.ooms.hr.payroll.dto.PayRollsDto;
import com.xdev.ooms.hr.pointage.dto.PointageDto;
import com.xdev.ooms.sharedkernel.Enum.Gender;
import com.xdev.ooms.sharedkernel.Enum.MaritalStatus;
import com.xdev.ooms.hr.employee.entity.Employee;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.time.LocalDate;
import java.util.List;

public class EmployeeDto extends BaseDto<Employee> {
    private String firstName;
    private String lastName;
    private Gender gender;
    private LocalDate birthDate;
    private String cin;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String country;
    private LocalDate hireDate;
    private String postalCode;
    private boolean active;
    private MaritalStatus maritalStatus;

    @JsonIgnoreProperties({"employee"})
    private List<ContractDto> contrats;

    @JsonIgnoreProperties({"employees","manager"})
    private DepartmentDto department;

    @JsonIgnoreProperties({"employee"})
    private List<PayRollsDto> payrolls;


    @JsonIgnoreProperties({"employee"})
    private List<PointageDto> pointages;

    //geter and seter

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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }


    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }


    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public List<ContractDto> getContrats() {
        return contrats;
    }

    public void setContrats(List<ContractDto> contrats) {
        this.contrats = contrats;
    }

    public DepartmentDto getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentDto department) {
        this.department = department;
    }

    public List<PayRollsDto> getPayrolls() {
        return payrolls;
    }

    public void setPayrolls(List<PayRollsDto> payrolls) {
        this.payrolls = payrolls;
    }

    public List<PointageDto> getPointages() {
        return pointages;
    }

    public void setPointages(List<PointageDto> pointages) {
        this.pointages = pointages;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}


