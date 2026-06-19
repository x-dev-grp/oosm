package com.xdev.ooms.inventory.materielsupplier.entity;

import com.xdev.ooms.inventory.Enum.MaterielSupplierCategory;
import com.xdev.ooms.sharedkernel.Enum.Currency;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

/**
 * Material / packaging supplier (inventory). Distinct from production {@code Supplier}
 * (olive farmers and oil sale clients under {@code /api/production/supplier}).
 */
@Entity

@Data
@Audited
@NoArgsConstructor
@AllArgsConstructor
public class MaterielSupplier extends BaseEntity {

    private String code;
    private String nom;
    private String nomCommercial;
    private String email;
    private String telephone;
    private String fax;
    private String siteWeb;
    private String numeroTva;
    private String adresse;
    private String ville;
    private String codePostal;
    private String pays;
    private String contactNom;
    private String contactPrenom;
    private String contactEmail;
    private String contactTelephone;
    private Currency currency;
    private Integer delaiLivraisonMoyen;
    private String conditionsPaiement;
    private Boolean actif = true;
    private String certifications;
    private java.time.LocalDateTime dateDerniereCommande;

    @Enumerated(EnumType.STRING)
    @Column(name = "categorie_fournisseur")
    private MaterielSupplierCategory category;

    public String displayName() {
        if (nomCommercial != null && !nomCommercial.isBlank()) {
            return nomCommercial.trim();
        }
        return nom == null ? "" : nom.trim();
    }
}
