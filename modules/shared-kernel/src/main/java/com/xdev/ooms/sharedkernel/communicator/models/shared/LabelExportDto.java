package com.xdev.ooms.sharedkernel.communicator.models.shared;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.xdev.ooms.sharedkernel.Enum.LabelClaimType;
import com.xdev.ooms.sharedkernel.Enum.LabelContentStatus;
import com.xdev.ooms.sharedkernel.Enum.LabelLanguage;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LabelExportDto implements Serializable {
    private UUID labelId;
    private LabelContentStatus status;
    private LabelLanguage language;
    private LocalDate packagingDate;
    private String legalDenomination;
    private String originCountry;
    private String netQuantity;
    private String bestBeforeDate;
    private String storageConditions;
    private String responsibleName;
    private String responsibleAddress;
    private String lotNumber;
    private String variety;
    private String qualityGrade;
    private String extractionMethod;
    private String sensoryProfile;
    private List<String> certifications = new ArrayList<>();
    private Set<LabelClaimType> claimTypes = new LinkedHashSet<>();
    private List<String> marketingClaims = new ArrayList<>();
    private boolean frozen;
    private String payloadJson;
    private LocalDateTime finalizedAt;
    private String finalizedBy;
    private String publicCode;


    public void setLabelId(UUID labelId) {
        this.labelId = labelId;
    }

    public LabelContentStatus getStatus() {
        return status;
    }

    public void setStatus(LabelContentStatus status) {
        this.status = status;
    }

    public LabelLanguage getLanguage() {
        return language;
    }

    public void setLanguage(LabelLanguage language) {
        this.language = language;
    }


    public void setPackagingDate(LocalDate packagingDate) {
        this.packagingDate = packagingDate;
    }


    public void setLegalDenomination(String legalDenomination) {
        this.legalDenomination = legalDenomination;
    }


    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }


    public void setNetQuantity(String netQuantity) {
        this.netQuantity = netQuantity;
    }



    public void setBestBeforeDate(String bestBeforeDate) {
        this.bestBeforeDate = bestBeforeDate;
    }


    public void setStorageConditions(String storageConditions) {
        this.storageConditions = storageConditions;
    }


    public void setResponsibleName(String responsibleName) {
        this.responsibleName = responsibleName;
    }


    public void setResponsibleAddress(String responsibleAddress) {
        this.responsibleAddress = responsibleAddress;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }


    public String getVariety() {
        return variety;
    }

    public void setVariety(String variety) {
        this.variety = variety;
    }

    public String getQualityGrade() {
        return qualityGrade;
    }

    public void setQualityGrade(String qualityGrade) {
        this.qualityGrade = qualityGrade;
    }

    public void setExtractionMethod(String extractionMethod) {
        this.extractionMethod = extractionMethod;
    }


    public void setSensoryProfile(String sensoryProfile) {
        this.sensoryProfile = sensoryProfile;
    }


    public void setCertifications(List<String> certifications) {
        this.certifications = certifications;
    }


    public void setClaimTypes(Set<LabelClaimType> claimTypes) {
        this.claimTypes = claimTypes;
    }


    public void setMarketingClaims(List<String> marketingClaims) {
        this.marketingClaims = marketingClaims;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }


    public void setFinalizedAt(LocalDateTime finalizedAt) {
        this.finalizedAt = finalizedAt;
    }



    public void setFinalizedBy(String finalizedBy) {
        this.finalizedBy = finalizedBy;
    }

    public String getPublicCode() {
        return publicCode;
    }

    public void setPublicCode(String publicCode) {
        this.publicCode = publicCode;
    }
}
