package com.xdev.ooms.conditioning.label.entity;

import  com.xdev.ooms.sharedkernel.Enum.LabelSourceType;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
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

    public LabelContent getLabelContent() {
        return labelContent;
    }

    public LabelSourceType getSourceType() {
        return sourceType;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public String getSourceBusinessKey() {
        return sourceBusinessKey;
    }

    public String getSnapshotJson() {
        return snapshotJson;
    }

    public void setLabelContent(LabelContent labelContent) {
        this.labelContent = labelContent;
    }

    public void setSourceType(LabelSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public void setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
    }

    public void setSourceBusinessKey(String sourceBusinessKey) {
        this.sourceBusinessKey = sourceBusinessKey;
    }

    public void setSnapshotJson(String snapshotJson) {
        this.snapshotJson = snapshotJson;
    }
}
