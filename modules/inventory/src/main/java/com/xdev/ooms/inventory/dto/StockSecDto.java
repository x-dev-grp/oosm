package com.xdev.ooms.inventory.dto;

import com.xdev.ooms.inventory.entity.StockSec;
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