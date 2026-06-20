package com.xdev.ooms.inventory.boncommande.entity;

import com.xdev.ooms.inventory.Enum.StatutBonCommande;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class BonCommande extends BaseEntity {

    @Column(name = "numero_bc", unique = true, nullable = false)
    private String numeroBC;

    @Column(name = "motif_refus")
    private String motifRefus;


    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @Column(name = "date_reception_prevue")
    private LocalDateTime dateReceptionPrevue;

    @Enumerated(EnumType.STRING)
    private StatutBonCommande status = StatutBonCommande.EN_ATTENTE;

    @OneToMany(mappedBy = "bonCommande", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<LigneBonCommande> lignes = new ArrayList<>();



    public BonCommande() {
    }

    public BonCommande(String numeroBC, String motifRefus, LocalDateTime dateValidation, LocalDateTime dateReceptionPrevue, StatutBonCommande status, List<LigneBonCommande> lignes) {
        this.numeroBC = numeroBC;
        this.motifRefus = motifRefus;
        this.dateValidation = dateValidation;
        this.dateReceptionPrevue = dateReceptionPrevue;
        this.status = status;
        this.lignes = lignes;
    }

    public String getNumeroBC() {
        return numeroBC;
    }

    public String getMotifRefus() {
        return motifRefus;
    }

    public LocalDateTime getDateValidation() {
        return dateValidation;
    }

    public LocalDateTime getDateReceptionPrevue() {
        return dateReceptionPrevue;
    }

    public StatutBonCommande getStatus() {
        return status;
    }

    public List<LigneBonCommande> getLignes() {
        return lignes;
    }

    public void setNumeroBC(String numeroBC) {
        this.numeroBC = numeroBC;
    }

    public void setMotifRefus(String motifRefus) {
        this.motifRefus = motifRefus;
    }

    public void setDateValidation(LocalDateTime dateValidation) {
        this.dateValidation = dateValidation;
    }

    public void setDateReceptionPrevue(LocalDateTime dateReceptionPrevue) {
        this.dateReceptionPrevue = dateReceptionPrevue;
    }

    public void setStatus(StatutBonCommande status) {
        this.status = status;
    }

    public void setLignes(List<LigneBonCommande> lignes) {
        this.lignes = lignes;
    }
}