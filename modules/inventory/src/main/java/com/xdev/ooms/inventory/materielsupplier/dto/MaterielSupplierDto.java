package com.xdev.ooms.inventory.materielsupplier.dto;

import com.xdev.ooms.inventory.Enum.MaterielSupplierCategory;
import com.xdev.ooms.inventory.materielsupplier.entity.MaterielSupplier;
import com.xdev.ooms.sharedkernel.Enum.Currency;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MaterielSupplierDto extends BaseDto<MaterielSupplier> implements Serializable {
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
    private MaterielSupplierCategory category;
    private Integer delaiLivraisonMoyen;
    private String conditionsPaiement;
    private Currency currency;
    private Boolean actif;
    private String certifications;
    private LocalDateTime dateDerniereCommande;
}
