package com.xdev.ooms.inventory.produitfinal.service;

import com.xdev.ooms.inventory.bom.dto.BOMDto;
import com.xdev.ooms.inventory.bom.service.BomService;
import com.xdev.ooms.inventory.common.service.InventoryDeleteGuardService;
import com.xdev.ooms.inventory.exception.InventoryBusinessException;
import com.xdev.ooms.inventory.produitfinal.dto.ProduitFinalDto;
import com.xdev.ooms.inventory.produitfinal.entity.ProduitFinal;
import com.xdev.ooms.inventory.Enum.ProduitFinalType;
import com.xdev.ooms.inventory.produitfinal.repository.ProduitFinalRepository;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.CampaignResolver;
import com.xdev.ooms.sharedkernel.ports.ProductLabelPort;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProduitFinalService extends BaseServiceImpl<ProduitFinal, ProduitFinalDto, ProduitFinalDto> {

    private final ProduitFinalRepository produitFinalRepository;
    private final ModelMapper modelMapper;
    private final InventoryDeleteGuardService deleteGuard;
    private final BomService bomService;
    private final ProductLabelPort productLabelPort;

    @Autowired
    public ProduitFinalService(BaseRepository<ProduitFinal> repository,
                               ProduitFinalRepository produitFinalRepository,
                               ModelMapper modelMapper,
                               InventoryDeleteGuardService deleteGuard,
                               BomService bomService,
                               @Autowired(required = false) ProductLabelPort productLabelPort) {
        super(repository, modelMapper);
        this.produitFinalRepository = produitFinalRepository;
        this.modelMapper = modelMapper;
        this.deleteGuard = deleteGuard;
        this.bomService = bomService;
        this.productLabelPort = productLabelPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProduitFinalDto> findAll() {
        return super.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public ProduitFinalDto findById(UUID id) {
        ProduitFinal produitFinal = produitFinalRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouve avec id: " + id));
        return toAggregateDto(produitFinal);
    }

    @Override
    @Transactional
    public ProduitFinalDto save(ProduitFinalDto produitFinalDto) {
        String name = resolveName(produitFinalDto);
        validateProductCompliance(produitFinalDto, true);
        String code = generateComplianceProductCode(produitFinalDto);
        ensureNameIsAvailable(name, null);
        ensureCodeIsAvailable(code, null);

        ProduitFinal produitFinal = new ProduitFinal();
        applyProduitFinalDto(produitFinal, produitFinalDto, name, code);
        produitFinal.setDeleted(false);
        if (produitFinal.getProductStatus() == null) {
            produitFinal.setProductStatus("DRAFT");
        }
        ProduitFinal savedProduitFinal = produitFinalRepository.save(produitFinal);
        syncProductAggregate(savedProduitFinal, produitFinalDto, true);
        return toAggregateDto(savedProduitFinal);
    }

    @Override
    @Transactional
    public ProduitFinalDto update(ProduitFinalDto produitFinalDto) {
        if (produitFinalDto == null || produitFinalDto.getId() == null) {
            throw new InventoryBusinessException("PRODUCT_ID_REQUIRED", "L'identifiant du produit est obligatoire");
        }

        ProduitFinal existingProduitFinal = produitFinalRepository.findByIdAndIsDeletedFalse(produitFinalDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouve avec id: " + produitFinalDto.getId()));
        String name = resolveName(produitFinalDto);
        String code = existingProduitFinal.getCode();
        validateProductCompliance(produitFinalDto, false);
        ensureNameIsAvailable(name, produitFinalDto.getId());
        ensureCodeIsAvailable(code, produitFinalDto.getId());
        applyProduitFinalDto(existingProduitFinal, produitFinalDto, name, code);

        ProduitFinal updatedProduitFinal = produitFinalRepository.save(existingProduitFinal);
        syncProductAggregate(updatedProduitFinal, produitFinalDto, false);
        return toAggregateDto(updatedProduitFinal);
    }

    @Override
    @Transactional
    public ProduitFinalDto delete(UUID id) {
        ProduitFinalDto dto = findById(id);
        softDeleteProduitFinal(id);
        return dto;
    }

    @Override
    @Transactional
    public void remove(UUID id) {
        softDeleteProduitFinal(id);
    }

    @Transactional(readOnly = true)
    public List<ProduitFinalDto> getAllActiveProduitsFinaux() {
        return produitFinalRepository.findByActifTrueAndIsDeletedFalse().stream()
                .map(produitFinal -> modelMapper.map(produitFinal, ProduitFinalDto.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProduitFinalDto> getProduitsFinauxByType(ProduitFinalType type) {
        return produitFinalRepository.findByTypeAndIsDeletedFalse(type).stream()
                .map(produitFinal -> modelMapper.map(produitFinal, ProduitFinalDto.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void desactiverProduitFinal(UUID id) {
        ProduitFinal produitFinal = produitFinalRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouve avec id: " + id));
        deleteGuard.assertProduitFinalCanBeRemoved(id);
        produitFinal.setActif(false);
        produitFinalRepository.save(produitFinal);
    }

    @Transactional
    public void activerProduitFinal(UUID id) {
        ProduitFinal produitFinal = produitFinalRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouve avec id: " + id));
        produitFinal.setActif(true);
        produitFinalRepository.save(produitFinal);
    }

    private void softDeleteProduitFinal(UUID id) {
        ProduitFinal produitFinal = produitFinalRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouve avec id: " + id));
        deleteGuard.assertProduitFinalCanBeRemoved(id);
        produitFinal.setDeleted(true);
        produitFinal.setActif(false);
        produitFinalRepository.save(produitFinal);
    }

    private void applyProduitFinalDto(ProduitFinal produitFinal, ProduitFinalDto produitFinalDto, String name, String code) {
        ProduitFinalType type = produitFinalDto.getType() == null ? ProduitFinalType.NON_VRAC : produitFinalDto.getType();

        produitFinal.setName(name);
        produitFinal.setCode(code);
        produitFinal.setType(type);
        produitFinal.setCategory(trimToNull(produitFinalDto.getCategory()));
        produitFinal.setUnitOfMeasure(resolveUnitOfMeasure(produitFinalDto, type));
        produitFinal.setDescription(trimToNull(produitFinalDto.getDescription()));
        produitFinal.setGrade(trimToNull(produitFinalDto.getGrade()));
        produitFinal.setOrigin(trimToNull(produitFinalDto.getOrigin()));
        produitFinal.setHarvestCampaign(resolveHarvestCampaign(produitFinalDto, produitFinal.getHarvestCampaign()));
        if (produitFinalDto.getActif() != null) {
            produitFinal.setActif(produitFinalDto.getActif());
        } else if (produitFinal.getActif() == null) {
            produitFinal.setActif(Boolean.TRUE);
        }

        if (type == ProduitFinalType.VRAC) {
            produitFinal.setVolume(null);
            produitFinal.setPackagingType(null);
            produitFinal.setBarcode(null);
            produitFinal.setUnitsPerCarton(null);
            produitFinal.setCartonsPerPallet(null);
            produitFinal.setNetWeight(null);
            produitFinal.setGrossWeight(null);
            produitFinal.setBrand(null);
            produitFinal.setDensity(produitFinalDto.getDensity());
            produitFinal.setStorageUnit(trimToNull(produitFinalDto.getStorageUnit()));
        } else {
            produitFinal.setVolume(produitFinalDto.getVolume());
            produitFinal.setPackagingType(trimToNull(produitFinalDto.getPackagingType()));
            produitFinal.setBarcode(trimToNull(produitFinalDto.getBarcode()));
            produitFinal.setUnitsPerCarton(produitFinalDto.getUnitsPerCarton());
            produitFinal.setCartonsPerPallet(produitFinalDto.getCartonsPerPallet());
            produitFinal.setNetWeight(produitFinalDto.getNetWeight());
            produitFinal.setGrossWeight(produitFinalDto.getGrossWeight());
            produitFinal.setBrand(trimToNull(produitFinalDto.getBrand()));
            produitFinal.setDensity(null);
            produitFinal.setStorageUnit(null);
        }

        applyComplianceFields(produitFinal, produitFinalDto);
    }

    private void applyComplianceFields(ProduitFinal produitFinal, ProduitFinalDto produitFinalDto) {
        produitFinal.setIngredientDeclaration(trimToNull(produitFinalDto.getIngredientDeclaration()));
        produitFinal.setStorageConditions(trimToNull(produitFinalDto.getStorageConditions()));
        produitFinal.setShelfLifeMonths(produitFinalDto.getShelfLifeMonths() != null ? produitFinalDto.getShelfLifeMonths() : 24);
        produitFinal.setAcidityLevel(trimToNull(produitFinalDto.getAcidityLevel()));
        produitFinal.setPeroxideValue(trimToNull(produitFinalDto.getPeroxideValue()));
        produitFinal.setK232(trimToNull(produitFinalDto.getK232()));
        produitFinal.setK270(trimToNull(produitFinalDto.getK270()));
        produitFinal.setPolyphenolContent(trimToNull(produitFinalDto.getPolyphenolContent()));
        produitFinal.setOliveVarieties(trimToNull(produitFinalDto.getOliveVarieties()));
        produitFinal.setHarvestRegion(trimToNull(produitFinalDto.getHarvestRegion()));
        produitFinal.setOrganic(Boolean.TRUE.equals(produitFinalDto.getOrganic()));
        produitFinal.setOrganicCertNumber(trimToNull(produitFinalDto.getOrganicCertNumber()));
        produitFinal.setOrganicCertBody(trimToNull(produitFinalDto.getOrganicCertBody()));
        produitFinal.setOrganicCertExpiry(produitFinalDto.getOrganicCertExpiry());
        produitFinal.setSupplierName(trimToNull(produitFinalDto.getSupplierName()));
        produitFinal.setSupplierCode(trimToNull(produitFinalDto.getSupplierCode()));
        produitFinal.setSupplierContact(trimToNull(produitFinalDto.getSupplierContact()));
        produitFinal.setOliveSourceType(trimToNull(produitFinalDto.getOliveSourceType()));
        produitFinal.setOliveSourceReference(trimToNull(produitFinalDto.getOliveSourceReference()));
        produitFinal.setProductionBatchRef(trimToNull(produitFinalDto.getProductionBatchRef()));
        produitFinal.setExtractionBatchRef(trimToNull(produitFinalDto.getExtractionBatchRef()));
        if (produitFinalDto.getProductStatus() != null) {
            produitFinal.setProductStatus(trimToNull(produitFinalDto.getProductStatus()));
        } else if (produitFinal.getProductStatus() == null) {
            produitFinal.setProductStatus("DRAFT");
        }
        produitFinal.setNutritionDeclarationJson(trimToNull(produitFinalDto.getNutritionDeclarationJson()));
        produitFinal.setBrandDescription(trimToNull(produitFinalDto.getBrandDescription()));
    }

    private void validateProductCompliance(ProduitFinalDto dto, boolean creating) {
        List<String> missing = new ArrayList<>();
        ProduitFinalType type = dto.getType() == null ? ProduitFinalType.NON_VRAC : dto.getType();
        String grade = trimToNull(dto.getGrade());

        if (trimToNull(dto.getName()) == null) {
            missing.add("Product Name");
        }
        if (grade == null) {
            missing.add("Product Category");
        }
        if (trimToNull(dto.getIngredientDeclaration()) == null) {
            missing.add("Ingredients");
        }
        if (trimToNull(dto.getStorageConditions()) == null) {
            missing.add("Storage Conditions");
        }

        if (requiresHarvestYear(grade) && trimToNull(dto.getHarvestCampaign()) == null) {
            missing.add("Harvest Year");
        }
        if (requiresAcidity(grade) && trimToNull(dto.getAcidityLevel()) == null) {
            missing.add("Acidity");
        }

        if (type == ProduitFinalType.NON_VRAC) {
            if (trimToNull(dto.getBrand()) == null) {
                missing.add("Brand");
            }
            if (trimToNull(dto.getOrigin()) == null) {
                missing.add("Origin");
            }
            if (trimToNull(dto.getPackagingType()) == null) {
                missing.add("Packaging Type");
            }
            if (dto.getVolume() == null || dto.getVolume() <= 0) {
                missing.add("Net Volume");
            }
            if (dto.getShelfLifeMonths() == null || dto.getShelfLifeMonths() < 1 || dto.getShelfLifeMonths() > 60) {
                missing.add("Shelf Life");
            }
            if (trimToNull(dto.getSupplierName()) == null) {
                missing.add("Supplier Name");
            }
            if (trimToNull(dto.getSupplierCode()) == null) {
                missing.add("Supplier Code");
            }
            if (trimToNull(dto.getOliveSourceType()) == null || trimToNull(dto.getOliveSourceReference()) == null) {
                missing.add("Olive Source");
            }
            if (trimToNull(dto.getProductionBatchRef()) == null) {
                missing.add("Production Batch");
            }
            if (trimToNull(dto.getExtractionBatchRef()) == null) {
                missing.add("Extraction Batch");
            }
            if (trimToNull(dto.getNutritionDeclarationJson()) == null) {
                missing.add("Nutritional Information");
            }
        }

        if (Boolean.TRUE.equals(dto.getOrganic())) {
            if (trimToNull(dto.getOrganicCertNumber()) == null) {
                missing.add("Organic Certificate Number");
            }
            if (trimToNull(dto.getOrganicCertBody()) == null) {
                missing.add("Certification Body");
            }
            if (dto.getOrganicCertExpiry() == null) {
                missing.add("Certification Expiration Date");
            } else if (dto.getOrganicCertExpiry().isBefore(LocalDate.now())) {
                throw new InventoryBusinessException(
                        "PRODUCT_ORGANIC_CERT_EXPIRED",
                        "Le certificat biologique est expire"
                );
            }
        }

        if (!missing.isEmpty()) {
            throw new InventoryBusinessException(
                    "PRODUCT_COMPLIANCE_FAILED",
                    "Product Approval Failed. Missing: " + String.join(", ", missing)
            );
        }
    }

    private boolean requiresHarvestYear(String grade) {
        return "EXTRA_VIRGIN".equals(grade) || "VIRGIN".equals(grade);
    }

    private boolean requiresAcidity(String grade) {
        return "EXTRA_VIRGIN".equals(grade) || "VIRGIN".equals(grade);
    }

    private String generateComplianceProductCode(ProduitFinalDto dto) {
        String prefix = resolveGradeCodePrefix(trimToNull(dto.getGrade()));
        if (prefix == null) {
            return generateBusinessCode("code", "PF");
        }

        int volumeMl = dto.getVolume() != null ? Math.round(dto.getVolume()) : 0;
        String base = prefix + "-" + volumeMl + "-";
        long nextSequence = produitFinalRepository.countByCodeStartingWithAndIsDeletedFalse(base) + 1;
        String code = String.format(Locale.ROOT, "%s%03d", base, nextSequence);
        if (code.length() > 50) {
            code = code.substring(0, 50);
        }
        return code;
    }

    private String resolveGradeCodePrefix(String grade) {
        if (grade == null) {
            return null;
        }
        return switch (grade) {
            case "EXTRA_VIRGIN" -> "EVOO";
            case "VIRGIN" -> "VOO";
            case "REFINED" -> "OO";
            case "POMACE" -> "POO";
            default -> null;
        };
    }

    private String resolveName(ProduitFinalDto produitFinalDto) {
        String name = trimToNull(produitFinalDto.getName());
        if (name == null) {
            name = trimToNull(produitFinalDto.getCode());
        }
        if (name == null) {
            throw new InventoryBusinessException("PRODUCT_NAME_REQUIRED", "Le nom du produit est obligatoire");
        }
        return name;
    }

    private String resolveUnitOfMeasure(ProduitFinalDto produitFinalDto, ProduitFinalType type) {
        String unitOfMeasure = trimToNull(produitFinalDto.getUnitOfMeasure());
        if (unitOfMeasure != null) {
            return unitOfMeasure;
        }
        return type == ProduitFinalType.VRAC ? "L" : "BOTTLE";
    }

    private String resolveHarvestCampaign(ProduitFinalDto produitFinalDto, String currentValue) {
        String harvestCampaign = trimToNull(produitFinalDto.getHarvestCampaign());
        if (harvestCampaign != null) {
            return harvestCampaign;
        }
        if (currentValue != null) {
            return currentValue;
        }
        return CampaignResolver.resolveCampaignLabel(LocalDate.now());
    }

    private void ensureNameIsAvailable(String name, UUID currentId) {
        produitFinalRepository.findByNameAndIsDeletedFalse(name)
                .filter(produitFinal -> currentId == null || !Objects.equals(produitFinal.getId(), currentId))
                .ifPresent(produitFinal -> {
                    throw new InventoryBusinessException("PRODUCT_NAME_EXISTS", "Un produit avec ce nom existe deja: " + name);
                });
    }

    private void ensureCodeIsAvailable(String code, UUID currentId) {
        produitFinalRepository.findByCodeAndIsDeletedFalse(code)
                .filter(produitFinal -> currentId == null || !Objects.equals(produitFinal.getId(), currentId))
                .ifPresent(produitFinal -> {
                    throw new InventoryBusinessException("PRODUCT_CODE_EXISTS", "Un produit avec ce code existe deja: " + code);
                });
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ProduitFinalDto toAggregateDto(ProduitFinal produitFinal) {
        ProduitFinalDto dto = modelMapper.map(produitFinal, ProduitFinalDto.class);
        if (produitFinal.getType() != ProduitFinalType.VRAC) {
            dto.setBom(bomService.getActiveBomForProduct(produitFinal.getId()));
        }
        if (productLabelPort != null) {
            dto.setLabels(productLabelPort.findLabelsByProductId(produitFinal.getId()));
        }
        return dto;
    }

    private void syncProductAggregate(ProduitFinal produitFinal, ProduitFinalDto produitFinalDto, boolean creating) {
        if (produitFinal.getType() == ProduitFinalType.VRAC) {
            return;
        }

        BOMDto bomDto = produitFinalDto.getBom();
        if (bomDto == null || bomDto.getLines() == null || bomDto.getLines().isEmpty()) {
            if (creating) {
                throw new InventoryBusinessException(
                        "PRODUCT_BOM_REQUIRED",
                        "Un produit conditionne doit posseder une nomenclature avec au moins un article"
                );
            }
            BOMDto existingBom = bomService.getActiveBomForProduct(produitFinal.getId());
            if (existingBom == null || existingBom.getLines() == null || existingBom.getLines().isEmpty()) {
                throw new InventoryBusinessException(
                        "PRODUCT_BOM_REQUIRED",
                        "Un produit conditionne doit posseder une nomenclature avec au moins un article"
                );
            }
        } else {
            bomDto.setFinalProductId(produitFinal.getId());
            bomService.createBom(bomDto);
        }

        if (productLabelPort != null && produitFinalDto.getLabelIds() != null && !produitFinalDto.getLabelIds().isEmpty()) {
            productLabelPort.linkLabelsToProduct(produitFinal.getId(), produitFinalDto.getLabelIds());
        }
    }
}
