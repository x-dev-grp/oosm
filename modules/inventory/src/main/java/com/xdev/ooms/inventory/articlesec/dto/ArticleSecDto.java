package com.xdev.ooms.inventory.articlesec.dto;

import com.xdev.ooms.inventory.Enum.CategorieArticle;

import com.xdev.ooms.inventory.articlesec.entity.ArticleSec;
import com.xdev.ooms.inventory.materielsupplier.dto.MaterielSupplierDto;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.models.UniteMesure;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Data
public class ArticleSecDto extends BaseDto<ArticleSec> implements Serializable {
  private  UUID id;
  private  UUID tenantId;
  private  Boolean isDeleted;
  private  String createdBy;
  private  LocalDateTime createdDate;
  private  String lastModifiedBy;
  private  LocalDateTime lastModifiedDate;
  private  UUID externalId;
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


}