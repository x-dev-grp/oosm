package com.xdev.ooms.inventory.articlesec.entity;

import com.xdev.ooms.inventory.materielsupplier.entity.MaterielSupplier;
import com.xdev.ooms.inventory.Enum.CategorieArticle;
import com.xdev.ooms.inventory.config.*;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import com.xdev.ooms.sharedkernel.models.UniteMesure;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class ArticleSec extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategorieArticle categorie;

    @ManyToOne
    @JoinColumn(name = "fournisseur_id")
    private MaterielSupplier materielSupplier;

    private Integer stockMinimum = 0;
    private Integer stockMaximum = 0;
    private Boolean actif = true;

    @Enumerated(EnumType.STRING)
    private UniteMesure um;

    // Dans com.xdev.ooms.inventory.articlesec.entity.ArticleSec
    @Column(name = "sku_id")
    private UUID skuId;

    @Column(name = "lot_created_date")
    private LocalDateTime lotCreatedDate;

    @Column(name = "lot_ddm")
    private LocalDate lotDdm;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private ArticleConfig configuration;
    public void validateConfiguration() {
        if (configuration == null) {
            throw new IllegalArgumentException("La configuration ne peut être nulle");
        }
        CategorieArticle configCategory = extractCategoryFromConfig(configuration);
        if (configCategory != this.categorie) {
            throw new IllegalArgumentException(
                    "Incohérence : la configuration est de type " + configCategory +
                            " mais l'article est catégorisé " + this.categorie
            );
        }
        validateBusinessRules();
    }

    private CategorieArticle extractCategoryFromConfig(ArticleConfig config) {
        if (config instanceof UniteConfig) return CategorieArticle.UNITE;
        if (config instanceof ColisConfig) return CategorieArticle.COLIS;
        if (config instanceof PaletteConfig) return CategorieArticle.PALETTE;
        if (config instanceof EmballageConfig) return CategorieArticle.EMBALLAGE;
        if (config instanceof ConsommableConfig) return CategorieArticle.CONSOMMABLE;
        throw new IllegalStateException("Type de configuration inconnu");
    }

        private void validateBusinessRules() {
            if (this.categorie == CategorieArticle.UNITE) {
                UniteConfig u = (UniteConfig) configuration;
                if (u.getVolumeMl() <= 0) {
                    throw new IllegalArgumentException("Volume unitaire invalide");
                }
            }

            if (this.categorie == CategorieArticle.COLIS) {
                ColisConfig c = (ColisConfig) configuration;
                if (c.getUnitsPerColis() <= 0) {
                    throw new IllegalArgumentException("Nombre d'unités par colis invalide");
                }
                if (c.getDimensions() == null) {
                    throw new IllegalArgumentException("Dimensions du colis requises");
                }
            }

            if (this.categorie == CategorieArticle.PALETTE) {
                PaletteConfig p = (PaletteConfig) configuration;
                if (p.getColisPerLayer() <= 0 || p.getNumberOfLayers() <= 0) {
                    throw new IllegalArgumentException("Configuration palette incomplète");
                }
                if (p.getColisId() == null) {
                    throw new IllegalArgumentException("La palette doit référencer un colis");
                }
            }

            if (this.categorie == CategorieArticle.EMBALLAGE) {
                EmballageConfig e = (EmballageConfig) configuration;
                if (e.getDimensions() == null && e.getPoidsGrammes() == null) {
                    throw new IllegalArgumentException("Emballage doit avoir au moins dimensions ou poids");
                }
            }
          }

    public ArticleSec() {
    }

    public ArticleSec(UUID id, String nom, CategorieArticle categorie, MaterielSupplier materielSupplier, Integer stockMinimum, Integer stockMaximum, Boolean actif, UniteMesure um, UUID skuId, LocalDateTime lotCreatedDate, LocalDate lotDdm, ArticleConfig configuration) {
        this.id = id;
        this.nom = nom;
        this.categorie = categorie;
        this.materielSupplier = materielSupplier;
        this.stockMinimum = stockMinimum;
        this.stockMaximum = stockMaximum;
        this.actif = actif;
        this.um = um;
        this.skuId = skuId;
        this.lotCreatedDate = lotCreatedDate;
        this.lotDdm = lotDdm;
        this.configuration = configuration;
    }

    public UUID getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public CategorieArticle getCategorie() {
        return categorie;
    }

    public MaterielSupplier getMaterielSupplier() {
        return materielSupplier;
    }

    public Integer getStockMinimum() {
        return stockMinimum;
    }

    public Integer getStockMaximum() {
        return stockMaximum;
    }

    public Boolean getActif() {
        return actif;
    }

    public UniteMesure getUm() {
        return um;
    }

    public UUID getSkuId() {
        return skuId;
    }

    public LocalDateTime getLotCreatedDate() {
        return lotCreatedDate;
    }

    public LocalDate getLotDdm() {
        return lotDdm;
    }

    public ArticleConfig getConfiguration() {
        return configuration;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setCategorie(CategorieArticle categorie) {
        this.categorie = categorie;
    }

    public void setMaterielSupplier(MaterielSupplier materielSupplier) {
        this.materielSupplier = materielSupplier;
    }

    public void setStockMinimum(Integer stockMinimum) {
        this.stockMinimum = stockMinimum;
    }

    public void setStockMaximum(Integer stockMaximum) {
        this.stockMaximum = stockMaximum;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public void setUm(UniteMesure um) {
        this.um = um;
    }

    public void setSkuId(UUID skuId) {
        this.skuId = skuId;
    }

    public void setLotCreatedDate(LocalDateTime lotCreatedDate) {
        this.lotCreatedDate = lotCreatedDate;
    }

    public void setLotDdm(LocalDate lotDdm) {
        this.lotDdm = lotDdm;
    }

    public void setConfiguration(ArticleConfig configuration) {
        this.configuration = configuration;
    }
}