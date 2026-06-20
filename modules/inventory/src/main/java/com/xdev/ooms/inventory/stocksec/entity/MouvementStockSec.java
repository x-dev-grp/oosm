package com.xdev.ooms.inventory.stocksec.entity;

import com.xdev.ooms.inventory.articlesec.entity.ArticleSec;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;
import com.xdev.ooms.inventory.Enum.TypeMouvement;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MouvementStockSec  extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleSec article;

    @Column(nullable = false)
    private Integer quantite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeMouvement typeMouvement;

    private String motif;

    private LocalDateTime dateMouvement;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

}