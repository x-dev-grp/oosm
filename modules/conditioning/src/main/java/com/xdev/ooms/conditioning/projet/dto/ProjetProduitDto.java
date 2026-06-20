package com.xdev.ooms.conditioning.projet.dto;

import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import java.util.UUID;

public class ProjetProduitDto extends BaseDto<com.xdev.ooms.conditioning.projet.entity.ProjetProduit> {
    private UUID projetId;
    private UUID productId;
    private UUID bomId;
    private Double quantiteCible;

    public UUID getProjetId() {
        return projetId;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getBomId() {
        return bomId;
    }

    public Double getQuantiteCible() {
        return quantiteCible;
    }

    public void setProjetId(UUID projetId) {
        this.projetId = projetId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public void setBomId(UUID bomId) {
        this.bomId = bomId;
    }

    public void setQuantiteCible(Double quantiteCible) {
        this.quantiteCible = quantiteCible;
    }
}
