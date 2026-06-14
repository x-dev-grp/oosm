package com.xdev.ooms.finance.oilcredit.entity;

import com.xdev.ooms.sharedkernel.basetype.entity.BaseType;
import  com.xdev.ooms.sharedkernel.Enum.CreditState;
import  com.xdev.ooms.sharedkernel.Enum.UnitType;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.UUID;

import static org.apache.commons.math3.util.Precision.round;

@Entity
public class OilCredit extends BaseEntity implements Serializable {

    private String emballage;

    // Default to 0d for safety
    private Double quantity = 0d;

    @Enumerated(EnumType.STRING)
    private UnitType unit;

    @ManyToOne(fetch = FetchType.LAZY)
    private BaseType oil_type;

    private UUID destinataire;
    private UUID transaction_id_in;
    private UUID transaction_id_out;

    @Enumerated(EnumType.STRING)
    private CreditState creditState = CreditState.PENDING;

    public UUID getTransaction_id_in() {
        return transaction_id_in;
    }

    public void setTransaction_id_in(UUID transaction_id_in) {
        this.transaction_id_in = transaction_id_in;
    }

    public UUID getTransaction_id_out() {
        return transaction_id_out;
    }

    public void setTransaction_id_out(UUID transaction_id_out) {
        this.transaction_id_out = transaction_id_out;
    }

    public String getEmballage() {
        return emballage;
    }

    public void setEmballage(String emballage) {
        // trim; allow null
        this.emballage = (emballage == null) ? null : emballage.trim();
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        // null -> 0d and round to 3 decimals
        this.quantity = (quantity == null) ? 0d : round(quantity, 3);
    }

    public UnitType getUnit() {
        return unit;
    }

    public void setUnit(UnitType unit) {
        this.unit = unit;
    }

    public BaseType getOil_type() {
        return oil_type;
    }

    public void setOil_type(BaseType oil_type) {
        this.oil_type = oil_type;
    }

    public UUID getDestinataire() {
        return destinataire;
    }

    public void setDestinataire(UUID destinataire) {
        this.destinataire = destinataire;
    }

    public CreditState getCreditState() {
        return creditState;
    }

    public void setCreditState(CreditState creditState) {
        this.creditState = creditState;
    }
}
