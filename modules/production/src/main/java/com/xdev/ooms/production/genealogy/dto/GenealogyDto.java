package com.xdev.ooms.production.genealogy.dto;


import com.xdev.ooms.production.filtration.dto.FiltrationStepDto;
import com.xdev.ooms.production.genealogy.dto.IntakeStepDto;
import com.xdev.ooms.production.genealogy.dto.RootSourceDto;
import com.xdev.ooms.production.genealogy.enums.TraceabilitySourceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GenealogyDto {
    private UUID traceabilityLotId;
    private String traceabilitySourceType;
    private UUID rootReceptionId;
    private UUID storageUnitId;
    private String lotNumber;
    private String storageUnitName;
    private Map<String, String> filteredQualityControls;

    private List<FiltrationStepDto> filtrations = new ArrayList<>();
    private List<RootSourceDto> rootSources = new ArrayList<>();
    /** Olive → oil reception → stock entry on the anchor storage unit (runtime). */
    private List<IntakeStepDto> intakeChain = new ArrayList<>();

    public UUID getTraceabilityLotId() {
        return traceabilityLotId;
    }

    public String getTraceabilitySourceType() {
        return traceabilitySourceType;
    }

    public UUID getRootReceptionId() {
        return rootReceptionId;
    }

    public UUID getStorageUnitId() {
        return storageUnitId;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public String getStorageUnitName() {
        return storageUnitName;
    }

    public Map<String, String> getFilteredQualityControls() {
        return filteredQualityControls;
    }

    public List<FiltrationStepDto> getFiltrations() {
        return filtrations;
    }

    public List<RootSourceDto> getRootSources() {
        return rootSources;
    }

    public List<IntakeStepDto> getIntakeChain() {
        return intakeChain;
    }

    public void setTraceabilityLotId(UUID traceabilityLotId) {
        this.traceabilityLotId = traceabilityLotId;
    }

    public void setTraceabilitySourceType(String traceabilitySourceType) {
        this.traceabilitySourceType = traceabilitySourceType;
    }

    public void setRootReceptionId(UUID rootReceptionId) {
        this.rootReceptionId = rootReceptionId;
    }

    public void setStorageUnitId(UUID storageUnitId) {
        this.storageUnitId = storageUnitId;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public void setStorageUnitName(String storageUnitName) {
        this.storageUnitName = storageUnitName;
    }

    public void setFilteredQualityControls(Map<String, String> filteredQualityControls) {
        this.filteredQualityControls = filteredQualityControls;
    }

    public void setFiltrations(List<FiltrationStepDto> filtrations) {
        this.filtrations = filtrations;
    }

    public void setRootSources(List<RootSourceDto> rootSources) {
        this.rootSources = rootSources;
    }

    public void setIntakeChain(List<IntakeStepDto> intakeChain) {
        this.intakeChain = intakeChain;
    }
}
