package com.xdev.ooms.finance.dto;

import com.xdev.ooms.finance.model.Supplier;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.communicator.models.shared.BaseTypeDto;

/**
 * DTO for {@link Supplier}
 */

public class SupplierDto extends BaseDto<Supplier> {
     private BaseTypeDto genericSupplierType;
    private Boolean hasStorage;
    private String name;
    private String lastname;
    private String phone;
    private String email;
    private String address;
    private BaseTypeDto region;
    private String rib;
    private String bankName;
    private String matriculeFiscal;

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


}
