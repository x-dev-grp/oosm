package com.xdev.ooms.inventory.common.service;

import com.xdev.ooms.inventory.Enum.StatutBonCommande;
import com.xdev.ooms.sharedkernel.communicator.models.shared.InventoryUsageBlockersDto;
import com.xdev.ooms.sharedkernel.ports.ConditioningUsagePort;
import com.xdev.ooms.inventory.stocksec.entity.StockSec;
import com.xdev.ooms.inventory.exception.InventoryBusinessException;
import com.xdev.ooms.inventory.articlesec.repository.ArticleSecRepository;
import com.xdev.ooms.inventory.bom.repository.BomLineRepository;
import com.xdev.ooms.inventory.bom.repository.BomRepository;
import com.xdev.ooms.inventory.boncommande.repository.LigneBonCommandeRepository;
import com.xdev.ooms.inventory.stocksec.repository.StockSecRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.UUID;

@Service
public class InventoryDeleteGuardService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryDeleteGuardService.class);

    private static final EnumSet<StatutBonCommande> OPEN_BON_COMMANDE_STATUSES = EnumSet.of(
            StatutBonCommande.EN_ATTENTE,
            StatutBonCommande.VALIDE,
            StatutBonCommande.PARTIELLEMENT_RECU
    );

    private final StockSecRepository stockSecRepository;
    private final BomLineRepository bomLineRepository;
    private final BomRepository bomRepository;
    private final LigneBonCommandeRepository ligneBonCommandeRepository;
    private final ArticleSecRepository articleSecRepository;
    private final ConditioningUsagePort conditioningUsagePort;

    public InventoryDeleteGuardService(StockSecRepository stockSecRepository,
                                       BomLineRepository bomLineRepository,
                                       BomRepository bomRepository,
                                       LigneBonCommandeRepository ligneBonCommandeRepository,
                                       ArticleSecRepository articleSecRepository,
                                       ConditioningUsagePort conditioningUsagePort) {
        this.stockSecRepository = stockSecRepository;
        this.bomLineRepository = bomLineRepository;
        this.bomRepository = bomRepository;
        this.ligneBonCommandeRepository = ligneBonCommandeRepository;
        this.articleSecRepository = articleSecRepository;
        this.conditioningUsagePort = conditioningUsagePort;
    }

    @Transactional(readOnly = true)
    public void assertArticleCanBeRemoved(UUID articleId) {
        stockSecRepository.findByArticleIdAndIsDeletedFalse(articleId)
                .ifPresent(this::assertStockCanBeRemoved);

        long bomUsage = bomLineRepository.countByArticle_IdAndIsDeletedFalseAndBom_IsDeletedFalse(articleId);
        if (bomUsage > 0) {
            throw new InventoryBusinessException(
                    "ARTICLE_USED_IN_BOM",
                    "Impossible de supprimer ou desactiver l'article : il est utilise dans une ou plusieurs nomenclatures actives"
            );
        }

        long openBonCommandeLines = ligneBonCommandeRepository.countByArticleIdAndBonCommandeStatusInAndIsDeletedFalse(
                articleId,
                OPEN_BON_COMMANDE_STATUSES
        );
        if (openBonCommandeLines > 0) {
            throw new InventoryBusinessException(
                    "ARTICLE_USED_IN_BON_COMMANDE",
                    "Impossible de supprimer ou desactiver l'article : il figure sur un bon de commande en cours"
            );
        }

        applyCrossModuleBlockers(fetchArticleUsageBlockers(articleId));
    }

    @Transactional(readOnly = true)
    public void assertProduitFinalCanBeRemoved(UUID productId) {
        long bomCount = bomRepository.countByProduitFinalIdAndIsDeletedFalse(productId);
        if (bomCount > 0) {
            throw new InventoryBusinessException(
                    "PRODUIT_HAS_BOM",
                    "Impossible de supprimer ou desactiver le produit : il possede une ou plusieurs nomenclatures actives"
            );
        }

        applyCrossModuleBlockers(fetchProductUsageBlockers(productId));
    }

    @Transactional(readOnly = true)
    public void assertFournisseurCanBeRemoved(UUID fournisseurId) {
        long articleCount = articleSecRepository.countByFournisseur_IdAndIsDeletedFalse(fournisseurId);
        if (articleCount > 0) {
            throw new InventoryBusinessException(
                    "FOURNISSEUR_HAS_ARTICLES",
                    "Impossible de supprimer ou desactiver le fournisseur : "
                            + articleCount + " article(s) y sont encore rattaches"
            );
        }
    }

    @Transactional(readOnly = true)
    public void assertStockCanBeRemoved(StockSec stock) {
        if (stock == null) {
            return;
        }

        int actuelle = safe(stock.getQuantiteActuelle());
        int reservee = safe(stock.getQuantiteReservee());
        if (actuelle > 0 || reservee > 0) {
            throw new InventoryBusinessException(
                    "STOCK_NOT_EMPTY",
                    "Impossible de supprimer le stock : quantite actuelle=" + actuelle + ", reserve=" + reservee
            );
        }

        if (stock.getEmplacement() != null) {
            throw new InventoryBusinessException(
                    "STOCK_ASSIGNED_EMPLACEMENT",
                    "Impossible de supprimer le stock : retirez-le d'abord de l'emplacement "
                            + labelEmplacement(stock)
            );
        }
    }

    private void applyCrossModuleBlockers(InventoryUsageBlockersDto blockers) {
        if (blockers == null || !blockers.isBlocked()) {
            return;
        }
        InventoryUsageBlockersDto.Blocker first = blockers.getBlockers().getFirst();
        throw new InventoryBusinessException(first.getCode(), first.getMessage());
    }

    private InventoryUsageBlockersDto fetchArticleUsageBlockers(UUID articleId) {
        try {
            return conditioningUsagePort.getArticleUsageBlockers(articleId);
        } catch (RuntimeException ex) {
            LOGGER.warn("Conditioning article usage check failed for {}: {}", articleId, ex.getMessage());
            throw usageCheckUnavailable();
        }
    }

    private InventoryUsageBlockersDto fetchProductUsageBlockers(UUID productId) {
        try {
            return conditioningUsagePort.getProductUsageBlockers(productId);
        } catch (RuntimeException ex) {
            LOGGER.warn("Conditioning product usage check failed for {}: {}", productId, ex.getMessage());
            throw usageCheckUnavailable();
        }
    }

    private InventoryBusinessException usageCheckUnavailable() {
        return new InventoryBusinessException(
                "USAGE_CHECK_UNAVAILABLE",
                "Impossible de verifier l'utilisation cross-module : service conditionnement indisponible"
        );
    }

    private static int safe(Integer value) {
        return value == null ? 0 : value;
    }

    private static String labelEmplacement(StockSec stock) {
        if (stock.getEmplacement().getNom() != null && !stock.getEmplacement().getNom().isBlank()) {
            return stock.getEmplacement().getNom();
        }
        return stock.getEmplacement().getCode() != null ? stock.getEmplacement().getCode() : stock.getEmplacement().getId().toString();
    }
}
