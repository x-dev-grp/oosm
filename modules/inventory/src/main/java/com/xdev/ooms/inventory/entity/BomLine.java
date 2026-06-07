package com.xdev.ooms.inventory.entity;

 import com.xdev.ooms.sharedkernel.entities.BaseEntity;
 import com.xdev.ooms.sharedkernel.models.UniteMesure;
 import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Getter
@Setter
@Audited
@Entity
public class BomLine extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "bom_id")
    private BOM bom;

    @ManyToOne
    @JoinColumn(name = "articleSec_id")
    private ArticleSec article;

    private double quantity;
    @Enumerated(EnumType.STRING)
    private UniteMesure unitOfMeasure;
}