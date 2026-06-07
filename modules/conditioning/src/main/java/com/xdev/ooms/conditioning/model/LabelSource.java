package com.xdev.ooms.conditioning.model;

import  com.xdev.ooms.sharedkernel.Enum.LabelSourceType;
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
public class LabelSource extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_content_id", nullable = false)
    private LabelContent labelContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LabelSourceType sourceType;

    @Column(name = "source_id")
    private UUID sourceId;

    private String sourceBusinessKey;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String snapshotJson;
}
