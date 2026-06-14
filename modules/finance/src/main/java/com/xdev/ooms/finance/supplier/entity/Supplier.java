package com.xdev.ooms.finance.supplier.entity;

import com.xdev.ooms.sharedkernel.basetype.entity.BaseType;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;

/**
 * A Supplier.
 */
@Entity(name = "FinanceSupplier")
@Table(name = "finance_supplier")
public class Supplier extends BaseEntity {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "generic_suppliertype_id")
    private BaseType genericSupplierType;
    private Boolean hasStorage = false;

    public Boolean getHasStorage() {
        return hasStorage;
    }

    public void setHasStorage(Boolean hasStorage) {
        this.hasStorage = hasStorage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRib() {
        return rib;
    }

    public void setRib(String rib) {
        this.rib = rib;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
    private String name;
    private String lastname;

    public BaseType getRegion() {
        return region;
    }

    public void setRegion(BaseType region) {
        this.region = region;
    }
    private String phone;
    private String email;
    private String address;
    private String rib;
    private String bankName;
    private String matriculeFiscal;



    @ManyToOne(fetch = FetchType.EAGER)
    private BaseType region;
    // Calculated fields (not persisted)
    @Transient
    private Float totalOliveQuantity;
    @Transient
    private Float totalOilQuantity;
    @Transient
    private Float totalPaidAmount;
    @Transient
    private Float totalUnpaidAmount;
    @Transient
    private Float totalDebt;

    public BaseType getGenericSupplierType() {
        return genericSupplierType;
    }

    public void setGenericSupplierType(BaseType genericSupplierType) {
        this.genericSupplierType = genericSupplierType;
    }

    public String getMatriculeFiscal() {
        return matriculeFiscal;
    }

    public void setMatriculeFiscal(String matriculeFiscal) {
        this.matriculeFiscal = matriculeFiscal;
    }

    public Float getTotalOliveQuantity() {
        return totalOliveQuantity;
    }

    public void setTotalOliveQuantity(Float totalOliveQuantity) {
        this.totalOliveQuantity = totalOliveQuantity;
    }

    public Float getTotalOilQuantity() {
        return totalOilQuantity;
    }

    public void setTotalOilQuantity(Float totalOilQuantity) {
        this.totalOilQuantity = totalOilQuantity;
    }

    public Float getTotalPaidAmount() {
        return totalPaidAmount;
    }

    public void setTotalPaidAmount(Float totalPaidAmount) {
        this.totalPaidAmount = totalPaidAmount;
    }

    public Float getTotalUnpaidAmount() {
        return totalUnpaidAmount;
    }

    public void setTotalUnpaidAmount(Float totalUnpaidAmount) {
        this.totalUnpaidAmount = totalUnpaidAmount;
    }

    public Float getTotalDebt() {
        return totalDebt;
    }

    public void setTotalDebt(Float totalDebt) {
        this.totalDebt = totalDebt;
    }
}
