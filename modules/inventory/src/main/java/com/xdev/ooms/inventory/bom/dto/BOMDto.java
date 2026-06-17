package com.xdev.ooms.inventory.bom.dto;

import com.xdev.ooms.inventory.bom.entity.BOM;
import com.xdev.ooms.inventory.produitfinal.entity.ProduitFinal;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
@Getter
@Setter

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
}
