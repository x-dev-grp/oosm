package com.xdev.ooms.conditioning.model;


import com.xdev.ooms.conditioning.Enum.ControlType;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.io.Serializable;
@Data
@Getter
@Setter
@Entity
@Audited
public class QCControlPoint extends BaseEntity implements Serializable {

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private QCPlan plan;
    private String nom;
    @Enumerated(EnumType.STRING)
    private ControlType type;
    private Double minValue;
    private Double maxValue;
    private boolean blocking;


}