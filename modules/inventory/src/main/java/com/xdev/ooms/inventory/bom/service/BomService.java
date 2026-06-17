package com.xdev.ooms.inventory.bom.service;

import com.xdev.ooms.inventory.Enum.ProduitFinalType;
import com.xdev.ooms.inventory.bom.dto.BOMDto;
import com.xdev.ooms.inventory.bom.dto.BomLineDto;
import com.xdev.ooms.inventory.exception.InventoryBusinessException;
import com.xdev.ooms.inventory.exception.ResourceNotFoundException;
import com.xdev.ooms.inventory.articlesec.entity.ArticleSec;
import com.xdev.ooms.inventory.bom.entity.BOM;
import com.xdev.ooms.inventory.bom.entity.BomLine;
import com.xdev.ooms.inventory.produitfinal.entity.ProduitFinal;
import com.xdev.ooms.inventory.articlesec.repository.ArticleSecRepository;
import com.xdev.ooms.inventory.bom.repository.BomRepository;
import com.xdev.ooms.inventory.produitfinal.repository.ProduitFinalRepository;
import com.xdev.ooms.sharedkernel.qr.CodeGenerator;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BomService extends BaseServiceImpl<BOM, BOMDto, BOMDto> {

    private final BomRepository bomRepository;
    private final ProduitFinalRepository produitFinalRepository;
    private final ArticleSecRepository articleRepository;

    @Autowired
    public BomService(BaseRepository<BOM> repository,
                      BomRepository bomRepository,
                      ProduitFinalRepository produitFinalRepository,
                      CodeGenerator codeGenerator,
                      ArticleSecRepository articleRepository,
                      ModelMapper modelMapper) {
        super(repository, codeGenerator, modelMapper);
        this.bomRepository = bomRepository;
        this.produitFinalRepository = produitFinalRepository;
        this.articleRepository = articleRepository;
    }

    @Override
    public Class<BOM> getEntityClass() {
        return BOM.class;
    }

    @Override
    @Transactional(readOnly = true)
    public BOMDto findById(UUID id) {
        BOM bom = bomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("BOM non trouvee avec l'id : " + id));
        return convertToDto(bom);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BOMDto> findAll() {
        return bomRepository.findAllByIsDeletedFalse().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BOMDto save(BOMDto bomDto) {
        return createBom(bomDto);
    }

    @Transactional(readOnly = true)
    public List<BOMDto> getBomsByProduct(UUID productId) {
        return bomRepository.findByProduitFinalIdAndIsDeletedFalse(productId).stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BOMDto getActiveBomForProduct(UUID productId) {
        return bomRepository.findFirstByProduitFinalIdAndActiveTrueAndIsDeletedFalse(productId)
                .or(() -> bomRepository.findFirstByProduitFinalIdAndIsDeletedFalse(productId))
                .map(this::convertToDto)
                .orElse(null);
    }

    @Transactional
    public BOMDto activateBom(UUID bomId) {
        BOM bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new ResourceNotFoundException("BOM non trouvee avec l'id : " + bomId));
        if (bom.getProduitFinal() == null) {
            throw new InventoryBusinessException("BOM_NO_PRODUCT", "Impossible d'activer une nomenclature sans produit");
        }
        UUID productId = bom.getProduitFinal().getId();
        List<BOM> siblings = bomRepository.findByProduitFinalId(productId);
        for (BOM other : siblings) {
            other.setActive(other.getId().equals(bomId));
        }
        return convertToDto(bomRepository.saveAll(siblings).stream()
                .filter(b -> b.getId().equals(bomId))
                .findFirst()
                .orElse(bom));
    }

    @Transactional
    public BOMDto createBom(BOMDto bomDto) {
        validateBomDto(bomDto, true);

        UUID finalProductId = resolveFinalProductId(bomDto);
        ProduitFinal produitFinal = produitFinalRepository.findById(finalProductId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouve avec l'id : " + finalProductId));
        if (produitFinal.getType() == ProduitFinalType.VRAC) {
            throw new InventoryBusinessException("BOM_VRAC_NOT_ALLOWED", "Une nomenclature emballage n'est pas applicable aux produits VRAC");
        }

        List<BOM> existingBoms = bomRepository.findByProduitFinalIdAndIsDeletedFalse(finalProductId);
        BOM bom = existingBoms.stream().findFirst().orElseGet(BOM::new);
        bom.setProduitFinal(produitFinal);
        bom.setVersion("V1");
        bom.setDeleted(false);
        bom.setActive(true);
        bom.getLines().clear();
        bom.setLines(buildLines(bom, bomDto.getLines()));

        for (BOM duplicate : existingBoms) {
            if (duplicate.getId() != null && !duplicate.getId().equals(bom.getId())) {
                duplicate.setActive(false);
                duplicate.setDeleted(true);
            }
        }
        if (existingBoms.size() > 1) {
            bomRepository.saveAll(existingBoms);
        }

        BOM saved = bomRepository.save(bom);
        return convertToDto(saved);
    }

    @Override
    @Transactional
    public BOMDto update(BOMDto bomDto) {
        if (bomDto == null || bomDto.getId() == null) {
            throw new InventoryBusinessException("BOM_ID_REQUIRED", "L'identifiant de la nomenclature est obligatoire");
        }
        return updateBom(bomDto.getId(), bomDto);
    }

    @Transactional
    public BOMDto updateBom(UUID id, BOMDto bomDto) {
        BOM bom = bomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("BOM non trouvee avec l'id : " + id));
        validateBomDto(bomDto, false);

        UUID finalProductId = resolveFinalProductId(bomDto);
        if (finalProductId == null) {
            throw new InventoryBusinessException("BOM_PRODUCT_REQUIRED", "Le produit fini est obligatoire");
        }

        ProduitFinal produitFinal = produitFinalRepository.findById(finalProductId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouve avec l'id : " + finalProductId));
        if (produitFinal.getType() == ProduitFinalType.VRAC) {
            throw new InventoryBusinessException("BOM_VRAC_NOT_ALLOWED", "Une nomenclature emballage n'est pas applicable aux produits VRAC");
        }

        bom.setProduitFinal(produitFinal);
        bom.setVersion("V1");
        bom.getLines().clear();
        bom.getLines().addAll(buildLines(bom, bomDto.getLines()));
        bom.setActive(true);

        List<BOM> siblings = bomRepository.findByProduitFinalIdAndIsDeletedFalse(produitFinal.getId());
        for (BOM other : siblings) {
            if (!other.getId().equals(bom.getId())) {
                other.setActive(false);
                other.setDeleted(true);
            }
        }
        if (!siblings.isEmpty()) {
            bomRepository.saveAll(siblings);
        }

        BOM updated = bomRepository.save(bom);
        return convertToDto(updated);
    }

    @Override
    @Transactional
    public BOMDto delete(UUID id) {
        BOMDto dto = findById(id);
        deleteBom(id);
        return dto;
    }

    @Override
    @Transactional
    public void remove(UUID id) {
        deleteBom(id);
    }

    @Transactional
    public void deleteBom(UUID id) {
        BOM bom = bomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BOM non trouvee avec l'id : " + id));
        bom.setActive(false);
        bom.setDeleted(true);
        bomRepository.save(bom);
    }

    private void validateBomDto(BOMDto bomDto, boolean creating) {
        if (resolveFinalProductId(bomDto) == null) {
            throw new InventoryBusinessException("BOM_PRODUCT_REQUIRED", "Le produit fini est obligatoire");
        }
        if (bomDto.getLines() == null || bomDto.getLines().isEmpty()) {
            throw new InventoryBusinessException("BOM_LINES_REQUIRED", "La nomenclature doit contenir au moins une ligne");
        }

        Set<UUID> articleIds = new HashSet<>();
        for (BomLineDto lineDto : bomDto.getLines()) {
            if (lineDto.getArticleId() == null) {
                throw new InventoryBusinessException("BOM_LINE_ARTICLE_REQUIRED", "Chaque ligne doit referencer un article");
            }
            if (!articleIds.add(lineDto.getArticleId())) {
                throw new InventoryBusinessException("BOM_DUPLICATE_ARTICLE", "Un article ne peut apparaitre qu'une seule fois dans la nomenclature");
            }
            if (lineDto.getQuantity() <= 0) {
                throw new InventoryBusinessException("BOM_LINE_QTY_INVALID", "La quantite par unite doit etre positive");
            }
            ArticleSec article = articleRepository.findById(lineDto.getArticleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Article non trouve avec l'id : " + lineDto.getArticleId()));
            if (!Boolean.TRUE.equals(article.getActif())) {
                throw new InventoryBusinessException(
                        "BOM_INACTIVE_ARTICLE",
                        "L'article " + article.getNom() + " est inactif et ne peut pas etre utilise"
                );
            }
        }
    }

    private UUID resolveFinalProductId(BOMDto bomDto) {
        return bomDto.getFinalProductId() != null ? bomDto.getFinalProductId() : bomDto.getProductId();
    }

    private List<BomLine> buildLines(BOM bom, List<BomLineDto> lineDtos) {
        return lineDtos.stream().map(lineDto -> {
            ArticleSec article = articleRepository.findById(lineDto.getArticleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Article non trouve avec l'id : " + lineDto.getArticleId()));
            BomLine line = new BomLine();
            line.setBom(bom);
            line.setArticle(article);
            line.setQuantity(lineDto.getQuantity());
            line.setUnitOfMeasure(article.getUm());
            return line;
        }).collect(Collectors.toList());
    }

    private BOMDto convertToDto(BOM bom) {
        BOMDto dto = new BOMDto();
        dto.setId(bom.getId());
        if (bom.getProduitFinal() != null) {
            dto.setFinalProductId(bom.getProduitFinal().getId());
            dto.setFinalProductName(bom.getProduitFinal().getName());
        } else {
            dto.setFinalProductName("Produit non assigne");
        }
        dto.setVersion("V1");
        dto.setActive(true);

        List<BomLineDto> lineDtos = bom.getLines().stream().map(line -> {
            BomLineDto lineDto = new BomLineDto();
            lineDto.setId(line.getId());
            if (line.getArticle() != null) {
                lineDto.setArticleId(line.getArticle().getId());
                lineDto.setArticleName(line.getArticle().getNom());
            } else {
                lineDto.setArticleName("Article inconnu");
            }
            lineDto.setQuantity(line.getQuantity());
            lineDto.setUnitOfMeasure(line.getUnitOfMeasure());
            return lineDto;
        }).collect(Collectors.toList());
        dto.setLines(lineDtos);
        return dto;
    }
}
