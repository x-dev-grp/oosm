package com.xdev.ooms.inventory.produitfinal.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xdev.ooms.inventory.produitfinal.entity.ProduitFinal;
import com.xdev.ooms.inventory.Enum.ProduitFinalType;
import com.xdev.ooms.inventory.bom.dto.BOMDto;
import com.xdev.ooms.sharedkernel.communicator.models.shared.LabelContentDto;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProduitFinalDto extends BaseDto<ProduitFinal> implements Serializable {
    UUID id;
    UUID tenantId;
    Boolean isDeleted;
    String createdBy;
    LocalDateTime createdDate;
    String lastModifiedBy;
    LocalDateTime lastModifiedDate;
    UUID externalId;
    String name;
    @JsonAlias("skuCode")
    String code;
    ProduitFinalType type = ProduitFinalType.NON_VRAC;
    String category;
    String unitOfMeasure;
    String description;
    String grade;
    String origin;
    String harvestCampaign;
    Float volume;
    String packagingType;
    String barcode;
    @JsonAlias("unitesParCols")
    Integer unitsPerCarton;
    @JsonAlias("colisParPalette")
    Integer cartonsPerPallet;
    Float netWeight;
    Float grossWeight;
    String brand;
    Float density;
    String storageUnit;
    private Boolean actif = true;

    private String ingredientDeclaration;
    private String storageConditions;
    private Integer shelfLifeMonths = 24;
    private String acidityLevel;
    private String peroxideValue;
    private String k232;
    private String k270;
    private String polyphenolContent;
    private String oliveVarieties;
    private String harvestRegion;
    private Boolean organic = false;
    private String organicCertNumber;
    private String organicCertBody;
    private LocalDate organicCertExpiry;
    private String supplierName;
    private String supplierCode;
    private String supplierContact;
    private String oliveSourceType;
    private String oliveSourceReference;
    private String productionBatchRef;
    private String extractionBatchRef;
    private String productStatus = "DRAFT";
    private String nutritionDeclarationJson;
    private String brandDescription;

    /** Single packaging BOM for NON_VRAC products (article lines). */
    private BOMDto bom;

    /** Label tickets linked to this product (read model). */
    private List<LabelContentDto> labels = new ArrayList<>();

    /** Existing label ticket ids to attach on save. */
    private List<UUID> labelIds = new ArrayList<>();

    @JsonProperty("unitesParCols")
    public Integer getUnitesParCols() {
        return unitsPerCarton;
    }

    public void setUnitesParCols(Integer unitesParCols) {
        this.unitsPerCarton = unitesParCols;
    }

    @JsonProperty("colisParPalette")
    public Integer getColisParPalette() {
        return cartonsPerPallet;
    }

    public void setColisParPalette(Integer colisParPalette) {
        this.cartonsPerPallet = colisParPalette;
    }

    public Boolean getActif() {
        return actif;
    }

    public String getIngredientDeclaration() {
        return ingredientDeclaration;
    }

    public String getStorageConditions() {
        return storageConditions;
    }

    public Integer getShelfLifeMonths() {
        return shelfLifeMonths;
    }

    public String getAcidityLevel() {
        return acidityLevel;
    }

    public String getPeroxideValue() {
        return peroxideValue;
    }

    public String getK232() {
        return k232;
    }

    public String getK270() {
        return k270;
    }

    public String getPolyphenolContent() {
        return polyphenolContent;
    }

    public String getOliveVarieties() {
        return oliveVarieties;
    }

    public String getHarvestRegion() {
        return harvestRegion;
    }

    public Boolean getOrganic() {
        return organic;
    }

    public String getOrganicCertNumber() {
        return organicCertNumber;
    }

    public String getOrganicCertBody() {
        return organicCertBody;
    }

    public LocalDate getOrganicCertExpiry() {
        return organicCertExpiry;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public String getSupplierContact() {
        return supplierContact;
    }

    public String getOliveSourceType() {
        return oliveSourceType;
    }

    public String getOliveSourceReference() {
        return oliveSourceReference;
    }

    public String getProductionBatchRef() {
        return productionBatchRef;
    }

    public String getExtractionBatchRef() {
        return extractionBatchRef;
    }

    public String getProductStatus() {
        return productStatus;
    }

    public String getNutritionDeclarationJson() {
        return nutritionDeclarationJson;
    }

    public String getBrandDescription() {
        return brandDescription;
    }

    public BOMDto getBom() {
        return bom;
    }

    public List<LabelContentDto> getLabels() {
        return labels;
    }

    public List<UUID> getLabelIds() {
        return labelIds;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public void setIngredientDeclaration(String ingredientDeclaration) {
        this.ingredientDeclaration = ingredientDeclaration;
    }

    public void setStorageConditions(String storageConditions) {
        this.storageConditions = storageConditions;
    }

    public void setShelfLifeMonths(Integer shelfLifeMonths) {
        this.shelfLifeMonths = shelfLifeMonths;
    }

    public void setAcidityLevel(String acidityLevel) {
        this.acidityLevel = acidityLevel;
    }

    public void setPeroxideValue(String peroxideValue) {
        this.peroxideValue = peroxideValue;
    }

    public void setK232(String k232) {
        this.k232 = k232;
    }

    public void setK270(String k270) {
        this.k270 = k270;
    }

    public void setPolyphenolContent(String polyphenolContent) {
        this.polyphenolContent = polyphenolContent;
    }

    public void setOliveVarieties(String oliveVarieties) {
        this.oliveVarieties = oliveVarieties;
    }

    public void setHarvestRegion(String harvestRegion) {
        this.harvestRegion = harvestRegion;
    }

    public void setOrganic(Boolean organic) {
        this.organic = organic;
    }

    public void setOrganicCertNumber(String organicCertNumber) {
        this.organicCertNumber = organicCertNumber;
    }

    public void setOrganicCertBody(String organicCertBody) {
        this.organicCertBody = organicCertBody;
    }

    public void setOrganicCertExpiry(LocalDate organicCertExpiry) {
        this.organicCertExpiry = organicCertExpiry;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public void setSupplierContact(String supplierContact) {
        this.supplierContact = supplierContact;
    }

    public void setOliveSourceType(String oliveSourceType) {
        this.oliveSourceType = oliveSourceType;
    }

    public void setOliveSourceReference(String oliveSourceReference) {
        this.oliveSourceReference = oliveSourceReference;
    }

    public void setProductionBatchRef(String productionBatchRef) {
        this.productionBatchRef = productionBatchRef;
    }

    public void setExtractionBatchRef(String extractionBatchRef) {
        this.extractionBatchRef = extractionBatchRef;
    }

    public void setProductStatus(String productStatus) {
        this.productStatus = productStatus;
    }

    public void setNutritionDeclarationJson(String nutritionDeclarationJson) {
        this.nutritionDeclarationJson = nutritionDeclarationJson;
    }

    public void setBrandDescription(String brandDescription) {
        this.brandDescription = brandDescription;
    }

    public void setBom(BOMDto bom) {
        this.bom = bom;
    }

    public void setLabels(List<LabelContentDto> labels) {
        this.labels = labels;
    }

    public void setLabelIds(List<UUID> labelIds) {
        this.labelIds = labelIds;
    }
}
