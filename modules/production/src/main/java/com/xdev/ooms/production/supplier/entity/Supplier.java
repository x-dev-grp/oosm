package com.xdev.ooms.production.supplier.entity;


import com.xdev.ooms.sharedkernel.basetype.entity.BaseType;
import com.xdev.ooms.production.storageunit.entity.StorageUnit;

import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

/**
 * A Supplier.
 */
@Getter
@Entity
public class Supplier extends BaseEntity {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "generic_suppliertype_id")
    private BaseType genericSupplierType;
    private Boolean hasStorage = false;
    private String name;
    private String lastname;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private String rib;
    private String bankName;
    private String matriculeFiscal;
    @OneToOne(mappedBy = "supplier", fetch = FetchType.LAZY)
    private StorageUnit storageUnit;
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

    public void setStorageUnit(StorageUnit storageUnit) {
        this.storageUnit = storageUnit;
    }

    public void setHasStorage(Boolean hasStorage) {
        this.hasStorage = hasStorage;
    }

    public void setMatriculeFiscal(String matriculeFiscal) {
        this.matriculeFiscal = matriculeFiscal;
    }

    public void setTotalOliveQuantity(Float totalOliveQuantity) {
        this.totalOliveQuantity = totalOliveQuantity;
    }

    /**
     * Sums the olive quantity from all olive deliveries.
     */

    public void setTotalOilQuantity(Float totalOilQuantity) {
        this.totalOilQuantity = totalOilQuantity;
    }

    /**
     * Sums the paid amounts from all oil deliveries.
     * (Assuming olive deliveries do not carry financial fields.)
     */


    public void setTotalPaidAmount(Float totalPaidAmount) {
        this.totalPaidAmount = totalPaidAmount;
    }

    /**
     * Sums the unpaid amounts from all oil deliveries.
     */


    public void setTotalUnpaidAmount(Float totalUnpaidAmount) {
        this.totalUnpaidAmount = totalUnpaidAmount;
    }

    /**
     * Calculates total debt (for example, as the difference between unpaid and paid amounts).
     */


    public void setTotalDebt(Float totalDebt) {
        this.totalDebt = totalDebt;
    }

    public void setName(String name) {
        this.name = name;
        refreshFullName();
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
        refreshFullName();
    }

    public void setFullName(String ignored) {
        refreshFullName();
    }

    public static String buildFullName(String name, String lastname) {
        String first = name == null ? "" : name.trim();
        String last = lastname == null ? "" : lastname.trim();
        String combined = (first + " " + last).trim();
        return combined.isEmpty() ? null : combined;
    }

    @PrePersist
    @PreUpdate
    private void onPersistOrUpdate() {
        refreshFullName();
    }

    @PostLoad
    private void ensureFullNameLoaded() {
        if (fullName == null && (name != null || lastname != null)) {
            refreshFullName();
        }
    }

    private void refreshFullName() {
        this.fullName = buildFullName(this.name, this.lastname);
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setRib(String rib) {
        this.rib = rib;
    }

    // Standard getters and setters for basic fields

    // Calculated totals based on child class values

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public void setRegion(BaseType region) {
        this.region = region;
    }

    public void setGenericSupplierType(BaseType genericSupplierType) {
        this.genericSupplierType = genericSupplierType;
    }


}
