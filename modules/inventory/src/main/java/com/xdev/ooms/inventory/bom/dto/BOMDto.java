package com.xdev.ooms.inventory.bom.dto;

import com.xdev.ooms.inventory.bom.entity.BOM;
import com.xdev.ooms.inventory.produitfinal.entity.ProduitFinal;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class BOMDto  extends BaseDto<BOM> implements Serializable {
    private List<BomLineDto> lines;
    private UUID finalProductId;
    private String finalProductName;
    private ProduitFinal produitFinal;
    private String version;
    private Boolean active;

    public UUID getProductId() {
        return finalProductId;
    }

    public void setProductId(UUID productId) {
        this.finalProductId = productId;
    }

    public String getProductName() {
        return finalProductName;
    }

    public void setProductName(String productName) {
        this.finalProductName = productName;
    }

    public UUID getSkuId() {
        return finalProductId;
    }

    public void setSkuId(UUID skuId) {
        this.finalProductId = skuId;
    }

    public String getSkuCode() {
        return finalProductName;
    }

    public void setSkuCode(String skuCode) {
        this.finalProductName = skuCode;
    }

    public List<BomLineDto> getLines() {
        return lines;
    }

    public UUID getFinalProductId() {
        return finalProductId;
    }

    public String getFinalProductName() {
        return finalProductName;
    }

    public ProduitFinal getProduitFinal() {
        return produitFinal;
    }

    public String getVersion() {
        return version;
    }

    public Boolean getActive() {
        return active;
    }

    public void setLines(List<BomLineDto> lines) {
        this.lines = lines;
    }

    public void setFinalProductId(UUID finalProductId) {
        this.finalProductId = finalProductId;
    }

    public void setFinalProductName(String finalProductName) {
        this.finalProductName = finalProductName;
    }

    public void setProduitFinal(ProduitFinal produitFinal) {
        this.produitFinal = produitFinal;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
