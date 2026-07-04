package com.xdev.ooms.inventory.produitfinal.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xdev.ooms.inventory.Enum.ProduitFinalType;
import com.xdev.ooms.inventory.bom.dto.BOMDto;
import com.xdev.ooms.inventory.produitfinal.entity.ProduitFinal;
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
    /**
     * Single packaging BOM for NON_VRAC products (article lines).
     */
    private BOMDto bom;
    /**
     * Label tickets linked to this product (read model).
     */
    private List<LabelContentDto> labels = new ArrayList<>();
    /**
     * Existing label ticket ids to attach on save.
     */
    private List<UUID> labelIds = new ArrayList<>();

    @Override
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    @Override
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public UUID getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public Boolean getDeleted() {
        return isDeleted;
    }

    @Override
    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    @Override
    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @Override
    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    @Override
    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ProduitFinalType getType() {
        return type;
    }

    public void setType(ProduitFinalType type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getHarvestCampaign() {
        return harvestCampaign;
    }

    public void setHarvestCampaign(String harvestCampaign) {
        this.harvestCampaign = harvestCampaign;
    }

    public Float getVolume() {
        return volume;
    }

    public void setVolume(Float volume) {
        this.volume = volume;
    }

    public String getPackagingType() {
        return packagingType;
    }

    public void setPackagingType(String packagingType) {
        this.packagingType = packagingType;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Integer getUnitsPerCarton() {
        return unitsPerCarton;
    }

    public void setUnitsPerCarton(Integer unitsPerCarton) {
        this.unitsPerCarton = unitsPerCarton;
    }

    public Integer getCartonsPerPallet() {
        return cartonsPerPallet;
    }

    public void setCartonsPerPallet(Integer cartonsPerPallet) {
        this.cartonsPerPallet = cartonsPerPallet;
    }

    public Float getNetWeight() {
        return netWeight;
    }

    public void setNetWeight(Float netWeight) {
        this.netWeight = netWeight;
    }

    public Float getGrossWeight() {
        return grossWeight;
    }

    public void setGrossWeight(Float grossWeight) {
        this.grossWeight = grossWeight;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Float getDensity() {
        return density;
    }

    public void setDensity(Float density) {
        this.density = density;
    }

    public String getStorageUnit() {
        return storageUnit;
    }

    public void setStorageUnit(String storageUnit) {
        this.storageUnit = storageUnit;
    }

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

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public String getIngredientDeclaration() {
        return ingredientDeclaration;
    }

    public void setIngredientDeclaration(String ingredientDeclaration) {
        this.ingredientDeclaration = ingredientDeclaration;
    }

    public String getStorageConditions() {
        return storageConditions;
    }

    public void setStorageConditions(String storageConditions) {
        this.storageConditions = storageConditions;
    }

    public Integer getShelfLifeMonths() {
        return shelfLifeMonths;
    }

    public void setShelfLifeMonths(Integer shelfLifeMonths) {
        this.shelfLifeMonths = shelfLifeMonths;
    }

    public String getAcidityLevel() {
        return acidityLevel;
    }

    public void setAcidityLevel(String acidityLevel) {
        this.acidityLevel = acidityLevel;
    }

    public String getPeroxideValue() {
        return peroxideValue;
    }

    public void setPeroxideValue(String peroxideValue) {
        this.peroxideValue = peroxideValue;
    }

    public String getK232() {
        return k232;
    }

    public void setK232(String k232) {
        this.k232 = k232;
    }

    public String getK270() {
        return k270;
    }

    public void setK270(String k270) {
        this.k270 = k270;
    }

    public String getPolyphenolContent() {
        return polyphenolContent;
    }

    public void setPolyphenolContent(String polyphenolContent) {
        this.polyphenolContent = polyphenolContent;
    }

    public String getOliveVarieties() {
        return oliveVarieties;
    }

    public void setOliveVarieties(String oliveVarieties) {
        this.oliveVarieties = oliveVarieties;
    }

    public String getHarvestRegion() {
        return harvestRegion;
    }

    public void setHarvestRegion(String harvestRegion) {
        this.harvestRegion = harvestRegion;
    }

    public Boolean getOrganic() {
        return organic;
    }

    public void setOrganic(Boolean organic) {
        this.organic = organic;
    }

    public String getOrganicCertNumber() {
        return organicCertNumber;
    }

    public void setOrganicCertNumber(String organicCertNumber) {
        this.organicCertNumber = organicCertNumber;
    }

    public String getOrganicCertBody() {
        return organicCertBody;
    }

    public void setOrganicCertBody(String organicCertBody) {
        this.organicCertBody = organicCertBody;
    }

    public LocalDate getOrganicCertExpiry() {
        return organicCertExpiry;
    }

    public void setOrganicCertExpiry(LocalDate organicCertExpiry) {
        this.organicCertExpiry = organicCertExpiry;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public String getSupplierContact() {
        return supplierContact;
    }

    public void setSupplierContact(String supplierContact) {
        this.supplierContact = supplierContact;
    }

    public String getOliveSourceType() {
        return oliveSourceType;
    }

    public void setOliveSourceType(String oliveSourceType) {
        this.oliveSourceType = oliveSourceType;
    }

    public String getOliveSourceReference() {
        return oliveSourceReference;
    }

    public void setOliveSourceReference(String oliveSourceReference) {
        this.oliveSourceReference = oliveSourceReference;
    }

    public String getProductionBatchRef() {
        return productionBatchRef;
    }

    public void setProductionBatchRef(String productionBatchRef) {
        this.productionBatchRef = productionBatchRef;
    }

    public String getExtractionBatchRef() {
        return extractionBatchRef;
    }

    public void setExtractionBatchRef(String extractionBatchRef) {
        this.extractionBatchRef = extractionBatchRef;
    }

    public String getProductStatus() {
        return productStatus;
    }

    public void setProductStatus(String productStatus) {
        this.productStatus = productStatus;
    }

    public String getNutritionDeclarationJson() {
        return nutritionDeclarationJson;
    }

    public void setNutritionDeclarationJson(String nutritionDeclarationJson) {
        this.nutritionDeclarationJson = nutritionDeclarationJson;
    }

    public String getBrandDescription() {
        return brandDescription;
    }

    public void setBrandDescription(String brandDescription) {
        this.brandDescription = brandDescription;
    }

    public BOMDto getBom() {
        return bom;
    }

    public void setBom(BOMDto bom) {
        this.bom = bom;
    }

    public List<LabelContentDto> getLabels() {
        return labels;
    }

    public void setLabels(List<LabelContentDto> labels) {
        this.labels = labels;
    }

    public List<UUID> getLabelIds() {
        return labelIds;
    }

    public void setLabelIds(List<UUID> labelIds) {
        this.labelIds = labelIds;
    }
}
