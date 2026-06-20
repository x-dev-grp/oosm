package com.xdev.ooms.inventory.produitfinal.entity;


import com.xdev.ooms.inventory.Enum.ProduitFinalType;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class ProduitFinal extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProduitFinalType type = ProduitFinalType.NON_VRAC;

    private String category;

    @Column(name = "unit_of_measure")
    private String unitOfMeasure;

    @Column(columnDefinition = "text")
    private String description;

    private String grade;

    private String origin;

    @Column(name = "harvest_campaign")
    private String harvestCampaign;

    private Float volume;

    @Column(name = "packaging_type")
    private String packagingType;

    private String barcode;

    @Column(name = "unites_par_cols")
    private Integer unitsPerCarton;

    @Column(name = "colis_par_palette")
    private Integer cartonsPerPallet;

    @Column(name = "net_weight")
    private Float netWeight;

    @Column(name = "gross_weight")
    private Float grossWeight;

    private String brand;

    private Float density;

    @Column(name = "storage_unit")
    private String storageUnit;

    private Boolean actif = true;

    @Column(name = "ingredient_declaration", columnDefinition = "text")
    private String ingredientDeclaration;

    @Column(name = "storage_conditions", columnDefinition = "text")
    private String storageConditions;

    @Column(name = "shelf_life_months")
    private Integer shelfLifeMonths = 24;

    @Column(name = "acidity_level")
    private String acidityLevel;

    @Column(name = "peroxide_value")
    private String peroxideValue;

    private String k232;

    private String k270;

    @Column(name = "polyphenol_content")
    private String polyphenolContent;

    @Column(name = "olive_varieties", columnDefinition = "text")
    private String oliveVarieties;

    @Column(name = "harvest_region")
    private String harvestRegion;

    private Boolean organic = false;

    @Column(name = "organic_cert_number")
    private String organicCertNumber;

    @Column(name = "organic_cert_body")
    private String organicCertBody;

    @Column(name = "organic_cert_expiry")
    private LocalDate organicCertExpiry;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "supplier_code")
    private String supplierCode;

    @Column(name = "supplier_contact")
    private String supplierContact;

    @Column(name = "olive_source_type")
    private String oliveSourceType;

    @Column(name = "olive_source_reference")
    private String oliveSourceReference;

    @Column(name = "production_batch_ref")
    private String productionBatchRef;

    @Column(name = "extraction_batch_ref")
    private String extractionBatchRef;

    @Column(name = "product_status")
    private String productStatus = "DRAFT";

    @Column(name = "nutrition_declaration_json", columnDefinition = "text")
    private String nutritionDeclarationJson;

    @Column(name = "brand_description", columnDefinition = "text")
    private String brandDescription;

    public Integer getUnitesParCols() {
        return unitsPerCarton;
    }

    public void setUnitesParCols(Integer unitesParCols) {
        this.unitsPerCarton = unitesParCols;
    }

    public Integer getColisParPalette() {
        return cartonsPerPallet;
    }

    public void setColisParPalette(Integer colisParPalette) {
        this.cartonsPerPallet = colisParPalette;
    }

    public ProduitFinal() {
    }

    public ProduitFinal(String name, String code, ProduitFinalType type, String category, String unitOfMeasure, String description, String grade, String origin, String harvestCampaign, Float volume, String packagingType, String barcode, Integer unitsPerCarton, Integer cartonsPerPallet, Float netWeight, Float grossWeight, String brand, Float density, String storageUnit, Boolean actif, String ingredientDeclaration, String storageConditions, Integer shelfLifeMonths, String acidityLevel, String peroxideValue, String k232, String k270, String polyphenolContent, String oliveVarieties, String harvestRegion, Boolean organic, String organicCertNumber, String organicCertBody, LocalDate organicCertExpiry, String supplierName, String supplierCode, String supplierContact, String oliveSourceType, String oliveSourceReference, String productionBatchRef, String extractionBatchRef, String productStatus, String nutritionDeclarationJson, String brandDescription) {
        this.name = name;
        this.code = code;
        this.type = type;
        this.category = category;
        this.unitOfMeasure = unitOfMeasure;
        this.description = description;
        this.grade = grade;
        this.origin = origin;
        this.harvestCampaign = harvestCampaign;
        this.volume = volume;
        this.packagingType = packagingType;
        this.barcode = barcode;
        this.unitsPerCarton = unitsPerCarton;
        this.cartonsPerPallet = cartonsPerPallet;
        this.netWeight = netWeight;
        this.grossWeight = grossWeight;
        this.brand = brand;
        this.density = density;
        this.storageUnit = storageUnit;
        this.actif = actif;
        this.ingredientDeclaration = ingredientDeclaration;
        this.storageConditions = storageConditions;
        this.shelfLifeMonths = shelfLifeMonths;
        this.acidityLevel = acidityLevel;
        this.peroxideValue = peroxideValue;
        this.k232 = k232;
        this.k270 = k270;
        this.polyphenolContent = polyphenolContent;
        this.oliveVarieties = oliveVarieties;
        this.harvestRegion = harvestRegion;
        this.organic = organic;
        this.organicCertNumber = organicCertNumber;
        this.organicCertBody = organicCertBody;
        this.organicCertExpiry = organicCertExpiry;
        this.supplierName = supplierName;
        this.supplierCode = supplierCode;
        this.supplierContact = supplierContact;
        this.oliveSourceType = oliveSourceType;
        this.oliveSourceReference = oliveSourceReference;
        this.productionBatchRef = productionBatchRef;
        this.extractionBatchRef = extractionBatchRef;
        this.productStatus = productStatus;
        this.nutritionDeclarationJson = nutritionDeclarationJson;
        this.brandDescription = brandDescription;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public ProduitFinalType getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public String getDescription() {
        return description;
    }

    public String getGrade() {
        return grade;
    }

    public String getOrigin() {
        return origin;
    }

    public String getHarvestCampaign() {
        return harvestCampaign;
    }

    public Float getVolume() {
        return volume;
    }

    public String getPackagingType() {
        return packagingType;
    }

    public String getBarcode() {
        return barcode;
    }

    public Integer getUnitsPerCarton() {
        return unitsPerCarton;
    }

    public Integer getCartonsPerPallet() {
        return cartonsPerPallet;
    }

    public Float getNetWeight() {
        return netWeight;
    }

    public Float getGrossWeight() {
        return grossWeight;
    }

    public String getBrand() {
        return brand;
    }

    public Float getDensity() {
        return density;
    }

    public String getStorageUnit() {
        return storageUnit;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setType(ProduitFinalType type) {
        this.type = type;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setHarvestCampaign(String harvestCampaign) {
        this.harvestCampaign = harvestCampaign;
    }

    public void setVolume(Float volume) {
        this.volume = volume;
    }

    public void setPackagingType(String packagingType) {
        this.packagingType = packagingType;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setUnitsPerCarton(Integer unitsPerCarton) {
        this.unitsPerCarton = unitsPerCarton;
    }

    public void setCartonsPerPallet(Integer cartonsPerPallet) {
        this.cartonsPerPallet = cartonsPerPallet;
    }

    public void setNetWeight(Float netWeight) {
        this.netWeight = netWeight;
    }

    public void setGrossWeight(Float grossWeight) {
        this.grossWeight = grossWeight;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setDensity(Float density) {
        this.density = density;
    }

    public void setStorageUnit(String storageUnit) {
        this.storageUnit = storageUnit;
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
}
