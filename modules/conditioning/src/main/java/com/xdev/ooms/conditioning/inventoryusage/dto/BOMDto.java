package com.xdev.ooms.conditioning.inventoryusage.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;
public class BOMDto {
    private UUID id;
    @JsonAlias("skuId")
    private UUID productId;
    private String productName;
    private String version;
    private List<BomLineDto> lines;
    private boolean active = false;

    @JsonProperty("skuId")
    public UUID getSkuId() {
        return productId;
    }

    public void setSkuId(UUID skuId) {
        this.productId = skuId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getVersion() {
        return version;
    }

    public List<BomLineDto> getLines() {
        return lines;
    }

    public boolean isActive() {
        return active;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setLines(List<BomLineDto> lines) {
        this.lines = lines;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
