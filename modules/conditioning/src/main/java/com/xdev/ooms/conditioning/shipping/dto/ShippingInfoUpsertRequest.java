package com.xdev.ooms.conditioning.shipping.dto;

import java.time.LocalDate;

public class ShippingInfoUpsertRequest {
    private String destination;
    private String incoterm;
    private String carrierName;
    private String driverName;
    private String truckNumber;
    private String trackingNumber;
    private LocalDate expectedShipDate;
    private String notes;

    public String getDestination() {
        return destination;
    }

    public String getIncoterm() {
        return incoterm;
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

    public LocalDate getExpectedShipDate() {
        return expectedShipDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setIncoterm(String incoterm) {
        this.incoterm = incoterm;
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

    public void setExpectedShipDate(LocalDate expectedShipDate) {
        this.expectedShipDate = expectedShipDate;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

