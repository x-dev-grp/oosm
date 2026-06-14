package com.xdev.ooms.production.qualitycontrol.entity;


import com.xdev.ooms.production.qualitycontrol.entity.QualityControlRule;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;

import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Setter
@Getter
@Entity
public class QualityControlResult extends BaseEntity implements Serializable {

    // The identifier of the quality rule (e.g., could be a foreign key from a rule table or a code)
    @ManyToOne(fetch = FetchType.LAZY)
    private QualityControlRule rule;

    // The value measured by the quality controller for this rule
    @Column(name = "measured_value", nullable = false)
    private String measuredValue;

    // Many-to-One relationship: each result is associated with one UnifiedDelivery.
    @ManyToOne(fetch = FetchType.LAZY)
    private UnifiedDelivery delivery;

    @Column(name = "filtration_operation_id")
    private UUID filtrationOperationId;

    @Column(name = "traceability_lot_id")
    private UUID traceabilityLotId;

    // Constructors
    public QualityControlResult() {
    }

    public QualityControlResult(QualityControlRule rule, String measuredValue, UnifiedDelivery delivery) {
        this.rule = rule;
        this.measuredValue = measuredValue;
        this.delivery = delivery;
    }


}
