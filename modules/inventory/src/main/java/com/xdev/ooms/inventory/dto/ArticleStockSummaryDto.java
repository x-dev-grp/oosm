package com.xdev.ooms.inventory.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ArticleStockSummaryDto {
    private UUID articleId;
    private Integer quantiteActuelle;
    private Integer quantiteReservee;
    private Integer quantiteDisponible;
    private Boolean belowMinimum;
}
