package com.xdev.ooms.inventory.dto;

import com.xdev.ooms.inventory.entity.ArticleSec;
import com.xdev.ooms.inventory.entity.BOM;
import com.xdev.ooms.inventory.entity.BomLine;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.models.UniteMesure;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Data
@Getter
public class BomLineDto  extends BaseDto<BomLine> implements Serializable {
    private BOM bom;
    private ArticleSec article;
    private UUID articleId;
    private String articleName;
    private double quantity;
    private UniteMesure unitOfMeasure;

}