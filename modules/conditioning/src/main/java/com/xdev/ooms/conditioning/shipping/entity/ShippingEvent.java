package com.xdev.ooms.conditioning.shipping.entity;

import com.xdev.ooms.conditioning.shipping.enums.ShippingEventType;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Audited
public class ShippingEvent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_info_id", nullable = false)
    private ShippingInfo shippingInfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ShippingEventType type;

    private LocalDateTime eventAt;

    @Column(length = 120)
    private String location;

    @Column(length = 1000)
    private String comment;
}

