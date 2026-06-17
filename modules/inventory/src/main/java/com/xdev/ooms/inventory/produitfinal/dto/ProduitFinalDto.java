package com.xdev.ooms.inventory.produitfinal.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xdev.ooms.inventory.produitfinal.entity.ProduitFinal;
import com.xdev.ooms.inventory.Enum.ProduitFinalType;
import com.xdev.ooms.inventory.bom.dto.BOMDto;
import com.xdev.ooms.sharedkernel.communicator.models.shared.LabelContentDto;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Data
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
}
