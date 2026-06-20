package com.xdev.ooms.conditioning.expedition.dto;

import java.time.LocalDate;

public class ExpeditionUpdateRequest {
    private String destination;
    private LocalDate plannedShipDate;
    private String notes;

    /* Transport */
    private String carrierName;
    private String driverName;
    private String truckNumber;
    private String trackingNumber;
    private String incoterm;

    public String getDestination() {
        return destination;
    }

    public LocalDate getPlannedShipDate() {
        return plannedShipDate;
    }

    public String getNotes() {
        return notes;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getTruckNumber() {
        return truckNumber;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getIncoterm() {
        return incoterm;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setPlannedShipDate(LocalDate plannedShipDate) {
        this.plannedShipDate = plannedShipDate;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public void setTruckNumber(String truckNumber) {
        this.truckNumber = truckNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public void setIncoterm(String incoterm) {
        this.incoterm = incoterm;
    }
}
