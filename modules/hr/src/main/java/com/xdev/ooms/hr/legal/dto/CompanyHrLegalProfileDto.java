package com.xdev.ooms.hr.legal.dto;

import com.xdev.ooms.hr.legal.entity.CompanyHrLegalProfile;
import com.xdev.ooms.hr.legal.enums.BusinessActivityType;
import com.xdev.ooms.hr.legal.enums.WeeklyRegimeType;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.math.BigDecimal;

public class CompanyHrLegalProfileDto extends BaseDto<CompanyHrLegalProfile> {
    private BusinessActivityType businessActivity;
    private String cnssEmployerNumber;
    private String employmentSector;
    private String cnssRegime;
    private String collectiveAgreement;
    private WeeklyRegimeType weeklyRegime;
    private BigDecimal accidentRiskRate;
    private String minimumWageProfile;
    private String fiscalRegime;
    private String notes;

    public BusinessActivityType getBusinessActivity() {
        return businessActivity;
    }

    public void setBusinessActivity(BusinessActivityType businessActivity) {
        this.businessActivity = businessActivity;
    }

    public String getCnssEmployerNumber() {
        return cnssEmployerNumber;
    }

    public void setCnssEmployerNumber(String cnssEmployerNumber) {
        this.cnssEmployerNumber = cnssEmployerNumber;
    }

    public String getEmploymentSector() {
        return employmentSector;
    }

    public void setEmploymentSector(String employmentSector) {
        this.employmentSector = employmentSector;
    }

    public String getCnssRegime() {
        return cnssRegime;
    }

    public void setCnssRegime(String cnssRegime) {
        this.cnssRegime = cnssRegime;
    }

    public String getCollectiveAgreement() {
        return collectiveAgreement;
    }

    public void setCollectiveAgreement(String collectiveAgreement) {
        this.collectiveAgreement = collectiveAgreement;
    }

    public WeeklyRegimeType getWeeklyRegime() {
        return weeklyRegime;
    }

    public void setWeeklyRegime(WeeklyRegimeType weeklyRegime) {
        this.weeklyRegime = weeklyRegime;
    }

    public BigDecimal getAccidentRiskRate() {
        return accidentRiskRate;
    }

    public void setAccidentRiskRate(BigDecimal accidentRiskRate) {
        this.accidentRiskRate = accidentRiskRate;
    }

    public String getMinimumWageProfile() {
        return minimumWageProfile;
    }

    public void setMinimumWageProfile(String minimumWageProfile) {
        this.minimumWageProfile = minimumWageProfile;
    }

    public String getFiscalRegime() {
        return fiscalRegime;
    }

    public void setFiscalRegime(String fiscalRegime) {
        this.fiscalRegime = fiscalRegime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
