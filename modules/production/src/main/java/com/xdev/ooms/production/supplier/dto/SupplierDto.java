package com.xdev.ooms.production.supplier.dto;

import com.xdev.ooms.production.storageunit.dto.StorageUnitDto;
import com.xdev.ooms.production.supplier.entity.Supplier;
import com.xdev.ooms.sharedkernel.basetype.dto.BaseTypeDto;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

/**
 * DTO for {@link Supplier}
 */
public class SupplierDto extends BaseDto<Supplier> {
    private BaseTypeDto genericSupplierType;
    private Boolean hasStorage;
    private String name;
    private String lastname;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private BaseTypeDto region;
    private String rib;
    private String bankName;
    private String matriculeFiscal;
    private StorageUnitDto storageUnit;

    public String getMatriculeFiscal() {
        return matriculeFiscal;
    }

    public void setMatriculeFiscal(String matriculeFiscal) {
        this.matriculeFiscal = matriculeFiscal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        refreshFullName();
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
        refreshFullName();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        if (fullName != null && !fullName.isBlank()) {
            this.fullName = fullName.trim();
            return;
        }
        refreshFullName();
    }

    private void refreshFullName() {
        this.fullName = Supplier.buildFullName(this.name, this.lastname);
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

    public BaseTypeDto getRegion() {
        return region;
    }

    public void setRegion(BaseTypeDto region) {
        this.region = region;
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

    public Boolean getHasStorage() {
        return hasStorage;
    }

    public void setHasStorage(Boolean hasStorage) {
        this.hasStorage = hasStorage;
    }

    public BaseTypeDto getGenericSupplierType() {
        return genericSupplierType;
    }

    public void setGenericSupplierType(BaseTypeDto genericSupplierType) {
        this.genericSupplierType = genericSupplierType;
    }

    public StorageUnitDto getStorageUnit() {
        return storageUnit;
    }

    public void setStorageUnit(StorageUnitDto storageUnit) {
        this.storageUnit = storageUnit;
    }
}
