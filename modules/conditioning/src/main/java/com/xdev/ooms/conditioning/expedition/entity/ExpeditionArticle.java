package com.xdev.ooms.conditioning.expedition.entity;

import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
public class ExpeditionArticle extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expedition_id", nullable = false)
    private Expedition expedition;

    @Column(name = "of_id")
    private UUID ofId;

    @Column(name = "of_code", length = 50)
    private String ofCode;

    @Column(name = "article_id")
    private UUID articleId;

    @Column(name = "article_name")
    private String articleName;

    @Column(name = "article_name_snapshot", length = 180)
    private String articleNameSnapshot;

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 18, scale = 3)
    private BigDecimal volume;

    @Column(name = "lot_number", length = 120)
    private String lotNumber;

    @Column(length = 30)
    private String unit;

    public Expedition getExpedition() {
        return expedition;
    }

    public UUID getOfId() {
        return ofId;
    }

    public String getOfCode() {
        return ofCode;
    }

    public UUID getArticleId() {
        return articleId;
    }

    public String getArticleName() {
        return articleName;
    }

    public String getArticleNameSnapshot() {
        return articleNameSnapshot;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public String getUnit() {
        return unit;
    }

    public void setExpedition(Expedition expedition) {
        this.expedition = expedition;
    }

    public void setOfId(UUID ofId) {
        this.ofId = ofId;
    }

    public void setOfCode(String ofCode) {
        this.ofCode = ofCode;
    }

    public void setArticleId(UUID articleId) {
        this.articleId = articleId;
    }

    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }

    public void setArticleNameSnapshot(String articleNameSnapshot) {
        this.articleNameSnapshot = articleNameSnapshot;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
