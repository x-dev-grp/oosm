package com.xdev.ooms.finance.dto;

import com.xdev.ooms.finance.model.OilCredit;
import com.xdev.ooms.production.dto.BaseTypeDto;
import com.xdev.ooms.sharedkernel.Enum.CreditState;
import com.xdev.ooms.sharedkernel.Enum.UnitType;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.util.UUID;

public class OilCreditDto extends BaseDto<OilCredit> {


    private String emballage;
    private UnitType unit;
    private Double quantity;
    private BaseTypeDto oil_type;
    private UUID destinataire;
    private UUID transaction_id_in;
    private UUID transaction_id_out;
    private CreditState creditState = CreditState.PENDING;

    public String getEmballage() {
        return emballage;
    }

    public void setEmballage(String emballage) {
        this.emballage = emballage;
    }

    public UnitType getUnit() {
        return unit;
    }

    public void setUnit(UnitType unit) {
        this.unit = unit;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public BaseTypeDto getOil_type() {
        return oil_type;
    }

    public void setOil_type(BaseTypeDto oil_type) {
        this.oil_type = oil_type;
    }

    public UUID getDestinataire() {
        return destinataire;
    }

    public void setDestinataire(UUID destinataire) {
        this.destinataire = destinataire;
    }

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

    public CreditState getCreditState() {
        return creditState;
    }

    public void setCreditState(CreditState creditState) {
        this.creditState = creditState;
    }
}
