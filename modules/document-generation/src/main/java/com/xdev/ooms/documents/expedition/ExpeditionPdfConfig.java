package com.xdev.ooms.documents.expedition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExpeditionPdfConfig {

    private String title;
    private String reference;
    private String date;
    private String clientName;
    private String clientAddress;
    private String clientPhone;
    private String destination;
    private String carrier;
    private String driver;
    private String truck;
    private String incoterm;
    private String companyAddress;
    private List<Line> lines = new ArrayList<>();
    private Map<String, Object> traceability;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getTruck() {
        return truck;
    }

    public void setTruck(String truck) {
        this.truck = truck;
    }

    public String getIncoterm() {
        return incoterm;
    }

    public void setIncoterm(String incoterm) {
        this.incoterm = incoterm;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines == null ? new ArrayList<>() : lines;
    }

    public Map<String, Object> getTraceability() {
        return traceability;
    }

    public void setTraceability(Map<String, Object> traceability) {
        this.traceability = traceability;
    }

    public static class Line {
        private String ofCode;
        private String articleName;
        private Integer quantity;
        private String unit;
        private String lotNumber;

        public String getOfCode() {
            return ofCode;
        }

        public void setOfCode(String ofCode) {
            this.ofCode = ofCode;
        }

        public String getArticleName() {
            return articleName;
        }

        public void setArticleName(String articleName) {
            this.articleName = articleName;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getLotNumber() {
            return lotNumber;
        }

        public void setLotNumber(String lotNumber) {
            this.lotNumber = lotNumber;
        }
    }
}
