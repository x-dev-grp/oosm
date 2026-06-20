package com.xdev.ooms.conditioning.inventoryusage.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xdev.ooms.conditioning.Enum.ProductType;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import java.util.UUID;

public class ProduitFinalDto extends BaseDto {
    private UUID id;
    private String name;
    @JsonAlias("skuCode")
    private String code;
    private ProductType type;
    private String category;
    private String unitOfMeasure;
    private String description;
    private String grade;
    private String origin;
    private String harvestCampaign;
    private Float volume;
    private String packagingType;
    private String barcode;
    @JsonAlias("unitesParCols")
    private Integer unitsPerCarton;
    @JsonAlias("colisParPalette")
    private Integer cartonsPerPallet;
    private Float netWeight;
    private Float grossWeight;
    private String brand;
    private Float density;
    private String storageUnit;
    private Boolean actif;

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

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public ProductType getType() {
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

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setType(ProductType type) {
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
}
