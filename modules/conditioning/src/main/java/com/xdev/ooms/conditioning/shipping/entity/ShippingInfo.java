package com.xdev.ooms.conditioning.shipping.entity;

import com.xdev.ooms.conditioning.projet.entity.Projet;
import com.xdev.ooms.conditioning.shipping.enums.ShippingStatus;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ShippingInfo extends BaseEntity {

    @Column(name = "shipping_number", nullable = false, unique = true, length = 80)
    private String shippingNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false, unique = true)
    private Projet projet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ShippingStatus status = ShippingStatus.DRAFT;

    @Column(length = 255)
    private String destination;

    @Column(length = 30)
    private String incoterm;

    private String carrierName;

    private String driverName;

    private String truckNumber;

    private String trackingNumber;

    private LocalDate expectedShipDate;

    private LocalDateTime departedAt;

    private LocalDateTime arrivedAt;

    private LocalDateTime deliveredAt;

    @Column(length = 2000)
    private String notes;

    @OneToMany(mappedBy = "shippingInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShippingLine> lines = new ArrayList<>();

    @OneToMany(mappedBy = "shippingInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShippingEvent> events = new ArrayList<>();

    public String getShippingNumber() {
        return shippingNumber;
    }

    public Projet getProjet() {
        return projet;
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

    public List<ShippingLine> getLines() {
        return lines;
    }

    public List<ShippingEvent> getEvents() {
        return events;
    }

    public void setShippingNumber(String shippingNumber) {
        this.shippingNumber = shippingNumber;
    }

    public void setProjet(Projet projet) {
        this.projet = projet;
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

    public void setLines(List<ShippingLine> lines) {
        this.lines = lines;
    }

    public void setEvents(List<ShippingEvent> events) {
        this.events = events;
    }
}

