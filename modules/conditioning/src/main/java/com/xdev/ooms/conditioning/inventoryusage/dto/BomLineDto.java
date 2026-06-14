package com.xdev.ooms.conditioning.inventoryusage.dto;

import com.xdev.ooms.inventory.articlesec.dto.ArticleSecDto;
import com.xdev.ooms.inventory.bom.dto.BOMDto;
import com.xdev.ooms.sharedkernel.models.UniteMesure;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Data
@Getter
@Setter
public class BomLineDto  {
    private UUID id;
    private double quantity;

    private BOMDto bom;
    private ArticleSecDto article;
    private UniteMesure unitOfMeasure;
}
