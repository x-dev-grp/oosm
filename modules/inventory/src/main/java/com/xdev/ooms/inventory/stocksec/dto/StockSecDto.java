package com.xdev.ooms.inventory.stocksec.dto;

import com.xdev.ooms.inventory.articlesec.dto.ArticleSecDto;
import com.xdev.ooms.inventory.emplacementstock.dto.EmplacementStockDto;
import com.xdev.ooms.inventory.stocksec.entity.StockSec;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;


@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StockSecDto extends BaseDto<StockSec> implements Serializable {

    private UUID articleId;
    private ArticleSecDto article;
    private Integer quantiteActuelle;
    private Integer quantiteReservee;
    private Integer quantiteDisponible;
    private UUID emplacementId;
    private EmplacementStockDto emplacement;
}