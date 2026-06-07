package com.xdev.ooms.production.dto;

import com.xdev.ooms.production.model.OilContainer;
import com.xdev.ooms.production.model.OilContainerSale;
import com.xdev.ooms.production.model.OilSale;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for {@link com.xdev.ooms.production.model.OilContainerSale}
 */
public class OilContainerSaleDto extends BaseDto<OilContainerSale> {
    UUID externalId;
    OilSaleDTO oilSale;
    OilContainerDTO container;
    Integer count;
    BigDecimal unitPrice;
    BigDecimal lineTotal;

    @Override
    public UUID getExternalId() {
        return externalId;
    }

    @Override
    public void setExternalId(UUID externalId) {
        this.externalId = externalId;
    }

    public OilSaleDTO getOilSale() {
        return oilSale;
    }

    public void setOilSale(OilSaleDTO oilSale) {
        this.oilSale = oilSale;
    }

    public OilContainerDTO getContainer() {
        return container;
    }

    public void setContainer(OilContainerDTO container) {
        this.container = container;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }
}