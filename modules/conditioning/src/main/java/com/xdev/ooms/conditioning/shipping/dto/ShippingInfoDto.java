package com.xdev.ooms.conditioning.shipping.dto;

import com.xdev.ooms.conditioning.shipping.entity.ShippingInfo;
import com.xdev.ooms.conditioning.shipping.enums.ShippingStatus;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShippingInfoDto extends BaseDto<ShippingInfo> {
    private String shippingNumber;
    private UUID projectId;
    private String projectCode;
    private ShippingStatus status;

    private String destination;
    private String incoterm;
    private String carrierName;
    private String driverName;
    private String truckNumber;
    private String trackingNumber;
    private LocalDate expectedShipDate;
    private LocalDateTime departedAt;
    private LocalDateTime arrivedAt;
    private LocalDateTime deliveredAt;
    private String notes;

    private String publicCode;
    private String qrImageBase64;

    private List<ShippingLineDto> lines = new ArrayList<>();
    private List<ShippingEventDto> events = new ArrayList<>();

    public String getShippingNumber() {
        return shippingNumber;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public ShippingStatus getStatus() {
        return status;
    }

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

    public LocalDateTime getDepartedAt() {
        return departedAt;
    }

    public LocalDateTime getArrivedAt() {
        return arrivedAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public String getNotes() {
        return notes;
    }

    public String getPublicCode() {
        return publicCode;
    }

    public String getQrImageBase64() {
        return qrImageBase64;
    }

    public List<ShippingLineDto> getLines() {
        return lines;
    }

    public List<ShippingEventDto> getEvents() {
        return events;
    }

    public void setShippingNumber(String shippingNumber) {
        this.shippingNumber = shippingNumber;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public void setStatus(ShippingStatus status) {
        this.status = status;
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

    public void setDepartedAt(LocalDateTime departedAt) {
        this.departedAt = departedAt;
    }

    public void setArrivedAt(LocalDateTime arrivedAt) {
        this.arrivedAt = arrivedAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setPublicCode(String publicCode) {
        this.publicCode = publicCode;
    }

    public void setQrImageBase64(String qrImageBase64) {
        this.qrImageBase64 = qrImageBase64;
    }

    public void setLines(List<ShippingLineDto> lines) {
        this.lines = lines;
    }

    public void setEvents(List<ShippingEventDto> events) {
        this.events = events;
    }
}
