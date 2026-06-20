package com.xdev.ooms.conditioning.projet.dto;


import com.xdev.ooms.conditioning.projet.enums.TypeEmballage;
import com.xdev.ooms.conditioning.projet.enums.TypeProduit;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class ProjetCreationRequest {

    @NotNull
    private UUID clientId;

    @NotNull
    private TypeProduit typeProduit;

    @NotNull
    private TypeEmballage typeEmballage;

    @Positive
    private Double quantiteCible;

    @NotBlank
    private String unite;  // "LITRES" ou "UNITES"

    @FutureOrPresent
    private LocalDate dateLimiteLivraison;

    @Positive
    private BigDecimal prixUnitaire;

    @NotBlank
    @Size(max = 2000)
    private String conditionsLivraison;

    public UUID getClientId() {
        return clientId;
    }

    public TypeProduit getTypeProduit() {
        return typeProduit;
    }

    public TypeEmballage getTypeEmballage() {
        return typeEmballage;
    }

    public Double getQuantiteCible() {
        return quantiteCible;
    }

    public String getUnite() {
        return unite;
    }

    public LocalDate getDateLimiteLivraison() {
        return dateLimiteLivraison;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public String getConditionsLivraison() {
        return conditionsLivraison;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public void setTypeProduit(TypeProduit typeProduit) {
        this.typeProduit = typeProduit;
    }

    public void setTypeEmballage(TypeEmballage typeEmballage) {
        this.typeEmballage = typeEmballage;
    }

    public void setQuantiteCible(Double quantiteCible) {
        this.quantiteCible = quantiteCible;
    }

    public void setUnite(String unite) {
        this.unite = unite;
    }

    public void setDateLimiteLivraison(LocalDate dateLimiteLivraison) {
        this.dateLimiteLivraison = dateLimiteLivraison;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public void setConditionsLivraison(String conditionsLivraison) {
        this.conditionsLivraison = conditionsLivraison;
    }
}