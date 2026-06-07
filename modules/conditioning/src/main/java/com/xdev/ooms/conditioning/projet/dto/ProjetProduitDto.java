package com.xdev.ooms.conditioning.projet.dto;

import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProjetProduitDto extends BaseDto<com.xdev.ooms.conditioning.projet.entity.ProjetProduit> {
    private UUID projetId;
    private UUID productId;
    private UUID bomId;
    private Double quantiteCible;
}
