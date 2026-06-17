package com.xdev.ooms.inventory.produitfinal.entity;


import com.xdev.ooms.inventory.Enum.ProduitFinalType;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.time.LocalDate;

@Audited
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
