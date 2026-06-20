package com.xdev.ooms.conditioning.label.entity;

import  com.xdev.ooms.sharedkernel.Enum.LabelCategory;
import  com.xdev.ooms.sharedkernel.Enum.LabelClaimType;
import  com.xdev.ooms.sharedkernel.Enum.LabelContentStatus;
import com.xdev.ooms.sharedkernel.Enum.LabelLanguage;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
public class LabelContent extends BaseEntity {

    @Column(name = "lot_id", nullable = false)
    private UUID lotId;

    @Column(name = "traceability_lot_id")
    private UUID traceabilityLotId;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "filtration_operation_id")
    private UUID filtrationOperationId;

    @Column(name = "packaging_id", nullable = false)
    private UUID packagingId;

    @Column(name = "operator_id", nullable = false)
    private UUID operatorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LabelContentStatus status = LabelContentStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LabelLanguage language = LabelLanguage.FR;

    @Column(name = "packaging_date", nullable = false)
    private LocalDate packagingDate;

    @Enumerated(EnumType.STRING)
    private LabelCategory labelCategory = LabelCategory.UNIT;

    private String legalDenomination;
    private String originCountry;
    private String netQuantity;
    private String bestBeforeDate;

    @Column(length = 1000)
    private String storageConditions;

    private String responsibleName;

    @Column(length = 1000)
    private String responsibleAddress;

    private String lotNumber;
    private String variety;
    private String qualityGrade;
    private String extractionMethod;
    private String sensoryProfile;

    @Column(length = 500)
    private String ingredientDeclaration;

    @Lob
    @Column(name = "nutrition_declaration_json", columnDefinition = "TEXT")
    private String nutritionDeclarationJson;

    @Column(length = 13)
    private String ean13;

    private String harvestYear;
    private String acidityLevel;
    private String brandName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "label_content_certifications", joinColumns = @JoinColumn(name = "label_content_id"))
    @Column(name = "certification")
    private List<String> certifications = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "label_content_claim_types", joinColumns = @JoinColumn(name = "label_content_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "claim_type")
    private Set<LabelClaimType> claimTypes = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "label_content_marketing_claims", joinColumns = @JoinColumn(name = "label_content_id"))
    @Column(name = "marketing_claim")
    private List<String> marketingClaims = new ArrayList<>();

    @Lob
    @Column(name = "final_payload_json", columnDefinition = "TEXT")
    private String finalPayloadJson;

    private LocalDateTime finalizedAt;
    private String finalizedBy;

    @OneToMany(mappedBy = "labelContent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("createdDate ASC")
    private List<LabelSource> sourceSnapshots = new ArrayList<>();

    public UUID getLotId() {
        return lotId;
    }

    public UUID getTraceabilityLotId() {
        return traceabilityLotId;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getFiltrationOperationId() {
        return filtrationOperationId;
    }

    public UUID getPackagingId() {
        return packagingId;
    }

    public UUID getOperatorId() {
        return operatorId;
    }

    public LabelContentStatus getStatus() {
        return status;
    }

    public LabelLanguage getLanguage() {
        return language;
    }

    public LocalDate getPackagingDate() {
        return packagingDate;
    }

    public LabelCategory getLabelCategory() {
        return labelCategory;
    }

    public String getLegalDenomination() {
        return legalDenomination;
    }

    public String getOriginCountry() {
        return originCountry;
    }

    public String getNetQuantity() {
        return netQuantity;
    }

    public String getBestBeforeDate() {
        return bestBeforeDate;
    }

    public String getStorageConditions() {
        return storageConditions;
    }

    public String getResponsibleName() {
        return responsibleName;
    }

    public String getResponsibleAddress() {
        return responsibleAddress;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public String getVariety() {
        return variety;
    }

    public String getQualityGrade() {
        return qualityGrade;
    }

    public String getExtractionMethod() {
        return extractionMethod;
    }

    public String getSensoryProfile() {
        return sensoryProfile;
    }

    public String getIngredientDeclaration() {
        return ingredientDeclaration;
    }

    public String getNutritionDeclarationJson() {
        return nutritionDeclarationJson;
    }

    public String getEan13() {
        return ean13;
    }

    public String getHarvestYear() {
        return harvestYear;
    }

    public String getAcidityLevel() {
        return acidityLevel;
    }

    public String getBrandName() {
        return brandName;
    }

    public List<String> getCertifications() {
        return certifications;
    }

    public Set<LabelClaimType> getClaimTypes() {
        return claimTypes;
    }

    public List<String> getMarketingClaims() {
        return marketingClaims;
    }

    public String getFinalPayloadJson() {
        return finalPayloadJson;
    }

    public LocalDateTime getFinalizedAt() {
        return finalizedAt;
    }

    public String getFinalizedBy() {
        return finalizedBy;
    }

    public List<LabelSource> getSourceSnapshots() {
        return sourceSnapshots;
    }

    public void setLotId(UUID lotId) {
        this.lotId = lotId;
    }

    public void setTraceabilityLotId(UUID traceabilityLotId) {
        this.traceabilityLotId = traceabilityLotId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public void setFiltrationOperationId(UUID filtrationOperationId) {
        this.filtrationOperationId = filtrationOperationId;
    }

    public void setPackagingId(UUID packagingId) {
        this.packagingId = packagingId;
    }

    public void setOperatorId(UUID operatorId) {
        this.operatorId = operatorId;
    }

    public void setStatus(LabelContentStatus status) {
        this.status = status;
    }

    public void setLanguage(LabelLanguage language) {
        this.language = language;
    }

    public void setPackagingDate(LocalDate packagingDate) {
        this.packagingDate = packagingDate;
    }

    public void setLabelCategory(LabelCategory labelCategory) {
        this.labelCategory = labelCategory;
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

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public void setVariety(String variety) {
        this.variety = variety;
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

    public void setIngredientDeclaration(String ingredientDeclaration) {
        this.ingredientDeclaration = ingredientDeclaration;
    }

    public void setNutritionDeclarationJson(String nutritionDeclarationJson) {
        this.nutritionDeclarationJson = nutritionDeclarationJson;
    }

    public void setEan13(String ean13) {
        this.ean13 = ean13;
    }

    public void setHarvestYear(String harvestYear) {
        this.harvestYear = harvestYear;
    }

    public void setAcidityLevel(String acidityLevel) {
        this.acidityLevel = acidityLevel;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
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

    public void setFinalPayloadJson(String finalPayloadJson) {
        this.finalPayloadJson = finalPayloadJson;
    }

    public void setFinalizedAt(LocalDateTime finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public void setFinalizedBy(String finalizedBy) {
        this.finalizedBy = finalizedBy;
    }

    public void setSourceSnapshots(List<LabelSource> sourceSnapshots) {
        this.sourceSnapshots = sourceSnapshots;
    }
}
