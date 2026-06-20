package com.xdev.ooms.production.genealogy.dto;



import java.util.Map;
import java.util.UUID;

public class RootSourceDto {
    private String type; // RECEPTION or TRITURATION
    private UUID sourceId;
    private String lotNumber;
    private String supplierName;
    private String date;
    private Map<String, Object> extra;
    private Map<String, String> qualityControls; // Key: ruleName, Value: measuredValue

    public String getType() {
        return type;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getDate() {
        return date;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public Map<String, String> getQualityControls() {
        return qualityControls;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public void setQualityControls(Map<String, String> qualityControls) {
        this.qualityControls = qualityControls;
    }
}
