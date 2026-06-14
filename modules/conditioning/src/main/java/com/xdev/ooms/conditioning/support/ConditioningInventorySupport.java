package com.xdev.ooms.conditioning.support;

import com.xdev.ooms.conditioning.inventoryusage.dto.*;
import com.xdev.ooms.conditioning.util.ConditioningInventoryMappers;
import com.xdev.ooms.inventory.articlesec.entity.ArticleSec;
import com.xdev.ooms.inventory.articlesec.repository.ArticleSecRepository;
import com.xdev.ooms.inventory.bom.entity.BOM;
import com.xdev.ooms.inventory.bom.repository.BomRepository;
import com.xdev.ooms.inventory.ligneconditionnement.entity.LigneConditionnement;
import com.xdev.ooms.inventory.ligneconditionnement.repository.LigneConditionnementRepository;
import com.xdev.ooms.inventory.produitfinal.entity.ProduitFinal;
import com.xdev.ooms.inventory.produitfinal.repository.ProduitFinalRepository;
import com.xdev.ooms.inventory.stocksec.service.StockSecService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConditioningInventorySupport {

    private final ProduitFinalRepository produitFinalRepository;
    private final ArticleSecRepository articleSecRepository;
    private final LigneConditionnementRepository ligneConditionnementRepository;
    private final BomRepository bomRepository;
    private final StockSecService stockSecService;
    private final ModelMapper modelMapper;

    public ProduitFinalDto getProduitFinalById(UUID id) {
        ProduitFinal entity = produitFinalRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouve avec id: " + id));
        return ConditioningInventoryMappers.toProduitFinalDto(entity, modelMapper);
    }

    public ArticleSecDto getArticleById(UUID id) {
        ArticleSec entity = articleSecRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Article non trouve avec id: " + id));
        return ConditioningInventoryMappers.toArticleSecDto(entity, modelMapper);
    }

    public LigneConditionnementDto getLigneById(UUID id) {
        LigneConditionnement entity = ligneConditionnementRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Ligne non trouvee avec id: " + id));
        return ConditioningInventoryMappers.toLigneDto(entity, modelMapper);
    }

    public BOMDto getBomById(UUID id) {
        BOM bom = bomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BOM non trouvee avec id: " + id));
        return ConditioningInventoryMappers.toBomDto(bom, modelMapper);
    }

    public BOMDto getActiveBomForProduct(UUID productId) {
        return bomRepository.findFirstByProduitFinalIdAndActiveTrue(productId)
                .map(bom -> ConditioningInventoryMappers.toBomDto(bom, modelMapper))
                .orElse(null);
    }

    public StockSecDto getStockByArticle(UUID articleId) {
        return ConditioningInventoryMappers.toStockSecDto(stockSecService.getStockByArticle(articleId), modelMapper);
    }

    public StockSecDto createStockForArticle(UUID articleId) {
        return ConditioningInventoryMappers.toStockSecDto(stockSecService.createStockForArticle(articleId), modelMapper);
    }

    public StockSecDto sortieStock(UUID articleId, Map<String, Object> payload) {
        return ConditioningInventoryMappers.toStockSecDto(
                stockSecService.sortieStock(
                        articleId,
                        ConditioningInventoryMappers.intFromPayload(payload, "quantite"),
                        ConditioningInventoryMappers.stringFromPayload(payload, "motif"),
                        ConditioningInventoryMappers.stringFromPayload(payload, "referenceType"),
                        ConditioningInventoryMappers.uuidFromPayload(payload, "referenceId")),
                modelMapper);
    }

    public StockSecDto reserverStock(UUID articleId, Map<String, Object> payload) {
        return ConditioningInventoryMappers.toStockSecDto(
                stockSecService.reserverStock(articleId, ConditioningInventoryMappers.intFromPayload(payload, "quantite")),
                modelMapper);
    }

    public StockSecDto annulerReservation(UUID articleId, Map<String, Object> payload) {
        return ConditioningInventoryMappers.toStockSecDto(
                stockSecService.annulerReservation(articleId, ConditioningInventoryMappers.intFromPayload(payload, "quantite")),
                modelMapper);
    }

    public StockSecDto consommerReservation(UUID articleId, Map<String, Object> payload) {
        return ConditioningInventoryMappers.toStockSecDto(
                stockSecService.consommerReservation(
                        articleId,
                        ConditioningInventoryMappers.intFromPayload(payload, "quantite"),
                        ConditioningInventoryMappers.stringFromPayload(payload, "motif"),
                        ConditioningInventoryMappers.stringFromPayload(payload, "referenceType"),
                        ConditioningInventoryMappers.uuidFromPayload(payload, "referenceId")),
                modelMapper);
    }
}
