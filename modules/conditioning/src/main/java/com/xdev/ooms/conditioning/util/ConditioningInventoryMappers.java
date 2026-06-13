package com.xdev.ooms.conditioning.util;

import com.xdev.ooms.conditioning.Enum.ProductType;
import com.xdev.ooms.conditioning.dto.ArticleSecDto;
import com.xdev.ooms.conditioning.dto.BOMDto;
import com.xdev.ooms.conditioning.dto.BomLineDto;
import com.xdev.ooms.conditioning.dto.LigneConditionnementDto;
import com.xdev.ooms.conditioning.dto.ProduitFinalDto;
import com.xdev.ooms.conditioning.dto.StockSecDto;
import com.xdev.ooms.inventory.entity.ArticleSec;
import com.xdev.ooms.inventory.entity.BOM;
import com.xdev.ooms.inventory.entity.BomLine;
import com.xdev.ooms.inventory.entity.LigneConditionnement;
import com.xdev.ooms.inventory.entity.ProduitFinal;
import org.modelmapper.ModelMapper;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ConditioningInventoryMappers {
    private ConditioningInventoryMappers() {
    }

    public static ProduitFinalDto toProduitFinalDto(ProduitFinal entity, ModelMapper modelMapper) {
        ProduitFinalDto dto = modelMapper.map(entity, ProduitFinalDto.class);
        if (entity.getType() != null) {
            dto.setType(ProductType.valueOf(entity.getType().name()));
        }
        return dto;
    }

    public static ArticleSecDto toArticleSecDto(ArticleSec entity, ModelMapper modelMapper) {
        ArticleSecDto dto = modelMapper.map(entity, ArticleSecDto.class);
        dto.setCategorie(entity.getCategorie() != null ? entity.getCategorie().name() : null);
        dto.setConfiguration(entity.getConfiguration() != null
                ? modelMapper.map(entity.getConfiguration(), Map.class)
                : null);
        return dto;
    }

    public static LigneConditionnementDto toLigneDto(LigneConditionnement entity, ModelMapper modelMapper) {
        return modelMapper.map(entity, LigneConditionnementDto.class);
    }

    public static BOMDto toBomDto(BOM bom, ModelMapper modelMapper) {
        BOMDto dto = new BOMDto();
        dto.setId(bom.getId());
        if (bom.getProduitFinal() != null) {
            dto.setProductId(bom.getProduitFinal().getId());
            dto.setProductName(bom.getProduitFinal().getName());
        } else {
            dto.setProductName("Produit non assigne");
        }
        dto.setVersion(bom.getVersion());
        dto.setActive(bom.isActive());
        dto.setLines(bom.getLines().stream()
                .map(line -> toBomLineDto(line, modelMapper))
                .collect(Collectors.toList()));
        return dto;
    }

    private static BomLineDto toBomLineDto(BomLine line, ModelMapper modelMapper) {
        BomLineDto lineDto = new BomLineDto();
        lineDto.setId(line.getId());
        lineDto.setArticle(modelMapper.map(line.getArticle(), com.xdev.ooms.inventory.dto.ArticleSecDto.class));

        lineDto.setQuantity(line.getQuantity());
        lineDto.setUnitOfMeasure(line.getUnitOfMeasure());
        return lineDto;
    }

    public static StockSecDto toStockSecDto(com.xdev.ooms.inventory.dto.StockSecDto stock, ModelMapper modelMapper) {
        return modelMapper.map(stock, StockSecDto.class);
    }

    public static Integer intFromPayload(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer integer) {
            return integer;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(value.toString());
    }

    public static String stringFromPayload(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value != null ? value.toString() : null;
    }

    public static UUID uuidFromPayload(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof UUID uuid) {
            return uuid;
        }
        return UUID.fromString(value.toString());
    }
}
