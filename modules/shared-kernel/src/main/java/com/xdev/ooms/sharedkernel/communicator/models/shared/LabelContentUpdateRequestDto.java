package com.xdev.ooms.sharedkernel.communicator.models.shared;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.xdev.ooms.sharedkernel.Enum.LabelClaimType;
import com.xdev.ooms.sharedkernel.Enum.LabelLanguage;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LabelContentUpdateRequestDto implements Serializable {
    private LabelLanguage language;
    private LocalDate packagingDate;
    private String legalDenomination;
    private String storageConditions;
    private String sensoryProfile;
    private List<String> certifications;
    private Set<LabelClaimType> claimTypes;

    public String getQualityGrade() {
        return qualityGrade;
    }

    public void setQualityGrade(String qualityGrade) {
        this.qualityGrade = qualityGrade;
    }

    public String getVariety() {
        return variety;
    }

    public void setVariety(String variety) {
        this.variety = variety;
    }

    private String qualityGrade;
    private String variety;
    // Additional editable fields
    private String lotNumber;
    private String originCountry;
    private String netQuantity;
    private String responsibleName;
    private String responsibleAddress;
    private String extractionMethod;
    private String bestBeforeDate;
    private List<String> marketingClaims;

    public LabelLanguage getLanguage() {
        return language;
    }

    public void setLanguage(LabelLanguage language) {
        this.language = language;
    }

    public LocalDate getPackagingDate() {
        return packagingDate;
    }

    public void setPackagingDate(LocalDate packagingDate) {
        this.packagingDate = packagingDate;
    }

    public String getLegalDenomination() {
        return legalDenomination;
    }

    public void setLegalDenomination(String legalDenomination) {
        this.legalDenomination = legalDenomination;
    }

    public String getStorageConditions() {
        return storageConditions;
    }

    public void setStorageConditions(String storageConditions) {
        this.storageConditions = storageConditions;
    }

    public String getSensoryProfile() {
        return sensoryProfile;
    }

    public void setSensoryProfile(String sensoryProfile) {
        this.sensoryProfile = sensoryProfile;
    }

    public List<String> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<String> certifications) {
        this.certifications = certifications;
    }

    public Set<LabelClaimType> getClaimTypes() {
        return claimTypes;
    }

    public void setClaimTypes(Set<LabelClaimType> claimTypes) {
        this.claimTypes = claimTypes;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public String getOriginCountry() {
        return originCountry;
    }

    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }

    public String getNetQuantity() {
        return netQuantity;
    }

    public void setNetQuantity(String netQuantity) {
        this.netQuantity = netQuantity;
    }

    public String getResponsibleName() {
        return responsibleName;
    }

    public void setResponsibleName(String responsibleName) {
        this.responsibleName = responsibleName;
    }

    public String getResponsibleAddress() {
        return responsibleAddress;
    }

    public void setResponsibleAddress(String responsibleAddress) {
        this.responsibleAddress = responsibleAddress;
    }

    public String getExtractionMethod() {
        return extractionMethod;
    }

    public void setExtractionMethod(String extractionMethod) {
        this.extractionMethod = extractionMethod;
    }

    public String getBestBeforeDate() {
        return bestBeforeDate;
    }

    public void setBestBeforeDate(String bestBeforeDate) {
        this.bestBeforeDate = bestBeforeDate;
    }

    public List<String> getMarketingClaims() {
        return marketingClaims;
    }

    public void setMarketingClaims(List<String> marketingClaims) {
        this.marketingClaims = marketingClaims;
    }
}
