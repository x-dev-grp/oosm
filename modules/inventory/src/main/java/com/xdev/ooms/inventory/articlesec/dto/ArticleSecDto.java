package com.xdev.ooms.inventory.articlesec.dto;

import com.xdev.ooms.inventory.Enum.CategorieArticle;

import com.xdev.ooms.inventory.articlesec.entity.ArticleSec;
import com.xdev.ooms.inventory.materielsupplier.dto.MaterielSupplierDto;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.models.UniteMesure;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class ArticleSecDto extends BaseDto<ArticleSec> implements Serializable {
  private  UUID id;
  private  UUID tenantId;
  private  Boolean isDeleted;
  private  String createdBy;
  private  LocalDateTime createdDate;
  private  String lastModifiedBy;
  private  LocalDateTime lastModifiedDate;
  private UniteMesure um;
  private  String nom;
  private  CategorieArticle categorie;
  private  Integer stockMinimum;
  private  Integer stockMaximum;
  private  Boolean actif;
  private MaterielSupplierDto materielSupplier;
  private String publicCode;
  private String qrUrl;
  private String qrImageBase64;
  private Map<String, Object> configuration;



    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public UniteMesure getUm() {
        return um;
    }

    public String getNom() {
        return nom;
    }

    public CategorieArticle getCategorie() {
        return categorie;
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

    public MaterielSupplierDto getMaterielSupplier() {
        return materielSupplier;
    }

    public String getPublicCode() {
        return publicCode;
    }

    public String getQrUrl() {
        return qrUrl;
    }

    public String getQrImageBase64() {
        return qrImageBase64;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public void setUm(UniteMesure um) {
        this.um = um;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setCategorie(CategorieArticle categorie) {
        this.categorie = categorie;
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

    public void setMaterielSupplier(MaterielSupplierDto materielSupplier) {
        this.materielSupplier = materielSupplier;
    }

    public void setPublicCode(String publicCode) {
        this.publicCode = publicCode;
    }

    public void setQrUrl(String qrUrl) {
        this.qrUrl = qrUrl;
    }

    public void setQrImageBase64(String qrImageBase64) {
        this.qrImageBase64 = qrImageBase64;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }
}