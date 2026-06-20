package com.xdev.ooms.production.filtration.dto;



import java.util.UUID;

public class FiltrationRequestDto {

    private UUID source;
    private UUID target;
    private Double volumeToFilter;
    private String note;





    public UUID getSource() {
        return source;
    }

    public UUID getTarget() {
        return target;
    }

    public Double getVolumeToFilter() {
        return volumeToFilter;
    }

    public String getNote() {
        return note;
    }

    public void setSource(UUID source) {
        this.source = source;
    }

    public void setTarget(UUID target) {
        this.target = target;
    }

    public void setVolumeToFilter(Double volumeToFilter) {
        this.volumeToFilter = volumeToFilter;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

