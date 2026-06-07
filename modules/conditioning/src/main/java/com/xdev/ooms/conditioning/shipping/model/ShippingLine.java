package com.xdev.ooms.conditioning.shipping.model;

import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.util.UUID;

@Entity
@Getter
@Setter
@Audited
public class ShippingLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_info_id", nullable = false)
    private ShippingInfo shippingInfo;

    private UUID articleId;

    private String articleNameSnapshot;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 30)
    private String unit;
}

