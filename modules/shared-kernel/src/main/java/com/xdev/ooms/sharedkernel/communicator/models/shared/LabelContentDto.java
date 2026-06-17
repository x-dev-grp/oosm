package com.xdev.ooms.sharedkernel.communicator.models.shared;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.xdev.ooms.sharedkernel.communicator.models.common.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.Enum.LabelCategory;
import com.xdev.ooms.sharedkernel.Enum.LabelClaimType;
import com.xdev.ooms.sharedkernel.Enum.LabelContentStatus;
import com.xdev.ooms.sharedkernel.Enum.LabelLanguage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LabelContentDto extends BaseDto {

    private LabelCategory labelCategory;
    private UUID lotId;
    private UUID traceabilityLotId;
    private UUID productId;
    private UUID packagingId;
    private UUID operatorId;
    private UUID filtrationOperationId;
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
    private String ingredientDeclaration;
    private String nutritionDeclarationJson;
    private String ean13;
    private String harvestYear;
    private String acidityLevel;
    private String brandName;
    private List<String> certifications = new ArrayList<>();
    private Set<LabelClaimType> claimTypes = new LinkedHashSet<>();
    private List<String> marketingClaims = new ArrayList<>();
    private String finalPayloadJson;
    private LocalDateTime finalizedAt;
    private String finalizedBy;
    private String publicCode;
    private List<LabelSourceSnapshotDto> sourceSnapshots = new ArrayList<>();
    private List<LabelValidationIssueDto> validationIssues = new ArrayList<>();

    public LabelCategory getLabelCategory() {
        return labelCategory;
    }

    public void setLabelCategory(LabelCategory labelCategory) {
        this.labelCategory = labelCategory;
    }

    public UUID getLotId() {
        return lotId;
    }

    public void setLotId(UUID lotId) {
        this.lotId = lotId;
    }

    public UUID getTraceabilityLotId() {
        return traceabilityLotId;
    }

    public void setTraceabilityLotId(UUID traceabilityLotId) {
        this.traceabilityLotId = traceabilityLotId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getPackagingId() {
        return packagingId;
    }

    public void setPackagingId(UUID packagingId) {
        this.packagingId = packagingId;
    }

    public UUID getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(UUID operatorId) {
        this.operatorId = operatorId;
    }

    public UUID getFiltrationOperationId() {
        return filtrationOperationId;
    }

    public void setFiltrationOperationId(UUID filtrationOperationId) {
        this.filtrationOperationId = filtrationOperationId;
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

    public String getBestBeforeDate() {
        return bestBeforeDate;
    }

    public void setBestBeforeDate(String bestBeforeDate) {
        this.bestBeforeDate = bestBeforeDate;
    }

    public String getStorageConditions() {
        return storageConditions;
    }

    public void setStorageConditions(String storageConditions) {
        this.storageConditions = storageConditions;
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

    public String getExtractionMethod() {
        return extractionMethod;
    }

    public void setExtractionMethod(String extractionMethod) {
        this.extractionMethod = extractionMethod;
    }

    public String getSensoryProfile() {
        return sensoryProfile;
    }

    public void setSensoryProfile(String sensoryProfile) {
        this.sensoryProfile = sensoryProfile;
    }

    public String getIngredientDeclaration() {
        return ingredientDeclaration;
    }

    public void setIngredientDeclaration(String ingredientDeclaration) {
        this.ingredientDeclaration = ingredientDeclaration;
    }

    public String getNutritionDeclarationJson() {
        return nutritionDeclarationJson;
    }

    public void setNutritionDeclarationJson(String nutritionDeclarationJson) {
        this.nutritionDeclarationJson = nutritionDeclarationJson;
    }

    public String getEan13() {
        return ean13;
    }

    public void setEan13(String ean13) {
        this.ean13 = ean13;
    }

    public String getHarvestYear() {
        return harvestYear;
    }

    public void setHarvestYear(String harvestYear) {
        this.harvestYear = harvestYear;
    }

    public String getAcidityLevel() {
        return acidityLevel;
    }

    public void setAcidityLevel(String acidityLevel) {
        this.acidityLevel = acidityLevel;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public List<String> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<String> certifications) {
        this.certifications = certifications != null ? certifications : new ArrayList<>();
    }

    public Set<LabelClaimType> getClaimTypes() {
        return claimTypes;
    }

    public void setClaimTypes(Set<LabelClaimType> claimTypes) {
        this.claimTypes = claimTypes != null ? claimTypes : new LinkedHashSet<>();
    }

    public List<String> getMarketingClaims() {
        return marketingClaims;
    }

    public void setMarketingClaims(List<String> marketingClaims) {
        this.marketingClaims = marketingClaims != null ? marketingClaims : new ArrayList<>();
    }

    public String getFinalPayloadJson() {
        return finalPayloadJson;
    }

    public void setFinalPayloadJson(String finalPayloadJson) {
        this.finalPayloadJson = finalPayloadJson;
    }

    public LocalDateTime getFinalizedAt() {
        return finalizedAt;
    }

    public void setFinalizedAt(LocalDateTime finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public String getFinalizedBy() {
        return finalizedBy;
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

    public List<LabelSourceSnapshotDto> getSourceSnapshots() {
        return sourceSnapshots;
    }

    public void setSourceSnapshots(List<LabelSourceSnapshotDto> sourceSnapshots) {
        this.sourceSnapshots = sourceSnapshots != null ? sourceSnapshots : new ArrayList<>();
    }

    public List<LabelValidationIssueDto> getValidationIssues() {
        return validationIssues;
    }

    public void setValidationIssues(List<LabelValidationIssueDto> validationIssues) {
        this.validationIssues = validationIssues != null ? validationIssues : new ArrayList<>();
    }
}
