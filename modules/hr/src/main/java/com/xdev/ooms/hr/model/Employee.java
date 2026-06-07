package com.xdev.ooms.hr.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.xdev.ooms.sharedkernel.Enum.Gender;
import com.xdev.ooms.sharedkernel.Enum.MaritalStatus;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Employee extends BaseEntity implements Serializable {

    private String firstName;
    private String lastName;
    private LocalDate hireDate;
    private LocalDate birthDate;
    private String cin;
    private String email;
    private String phone;
    private String address;
    private String postalCode;
    private String city;
    private String country;
    private boolean active=false;


    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;



    //relation avec contrat
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Contract> contrats;

    // Relation avec Department
 //  @OneToOne(mappedBy = "manager")
   //@JsonBackReference
    //private Department managedDepartment;

    @ManyToOne
    @JoinColumn(name = "department_id")
    @JsonBackReference
    private Department department;

    // Relation avec Payroll
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<PayRolls> payrolls;
    // Relation avec Pointage

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<Pointage> pointages;


    //geter et seter
    public List<Contract> getContrats() {
        return contrats;
    }

    public void setContrats(List<Contract> contrats) {
        this.contrats = contrats;
    }

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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public List<PayRolls> getPayrolls() {
        return payrolls;
    }

    public void setPayrolls(List<PayRolls> payrolls) {
        this.payrolls = payrolls;
    }

    public List<Pointage> getPointages() {
        return pointages;
    }

    public void setPointages(List<Pointage> pointages) {
        this.pointages = pointages;
    }

    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
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


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}