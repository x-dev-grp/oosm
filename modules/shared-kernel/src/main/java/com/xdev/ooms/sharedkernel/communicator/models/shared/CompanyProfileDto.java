package com.xdev.ooms.sharedkernel.communicator.models.shared;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.xdev.ooms.sharedkernel.communicator.models.common.dtos.BaseDto;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyProfileDto extends BaseDto {
    private String legalName;

    private String email;

    private String addressLine1;
    private String city;
    private String postalCode;
    private String governorate;


    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getGovernorate() {
        return governorate;
    }

    public void setGovernorate(String governorate) {
        this.governorate = governorate;
    }


}
