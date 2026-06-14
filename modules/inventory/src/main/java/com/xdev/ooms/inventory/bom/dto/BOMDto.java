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
    private UUID productId;
    private String productName;
    private ProduitFinal produitFinal;
    private String version;
    private Boolean active;

    public UUID getSkuId() {
        return productId;
    }

    public void setSkuId(UUID skuId) {
        this.productId = skuId;
    }

    public String getSkuCode() {
        return productName;
    }

    public void setSkuCode(String skuCode) {
        this.productName = skuCode;
    }
}
