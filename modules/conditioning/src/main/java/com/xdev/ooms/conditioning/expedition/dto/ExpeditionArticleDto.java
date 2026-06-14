package com.xdev.ooms.conditioning.expedition.dto;

import com.xdev.ooms.conditioning.expedition.entity.ExpeditionArticle;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ExpeditionArticleDto extends BaseDto<ExpeditionArticle> {
    private UUID ofId;
    private String ofCode;
    private UUID articleId;
    private String articleName;
    private Integer quantity;
    private BigDecimal volume;
    private String lotNumber;
    private String unit;
}
