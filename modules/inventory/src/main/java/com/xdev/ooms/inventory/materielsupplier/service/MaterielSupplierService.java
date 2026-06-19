package com.xdev.ooms.inventory.materielsupplier.service;

import com.xdev.ooms.inventory.common.service.InventoryDeleteGuardService;
import com.xdev.ooms.inventory.materielsupplier.dto.MaterielSupplierDto;
import com.xdev.ooms.inventory.materielsupplier.entity.MaterielSupplier;
import com.xdev.ooms.inventory.materielsupplier.repository.MaterielSupplierRepository;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MaterielSupplierService extends BaseServiceImpl<MaterielSupplier, MaterielSupplierDto, MaterielSupplierDto> {

    private final MaterielSupplierRepository materielSupplierRepository;
    private final ModelMapper modelMapper;
    private final InventoryDeleteGuardService deleteGuard;

    @Autowired
    public MaterielSupplierService(BaseRepository<MaterielSupplier> repository,
                                   MaterielSupplierRepository materielSupplierRepository,
                                   ModelMapper modelMapper,
                                   InventoryDeleteGuardService deleteGuard) {
        super(repository, modelMapper);
        this.materielSupplierRepository = materielSupplierRepository;
        this.modelMapper = modelMapper;
        this.deleteGuard = deleteGuard;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterielSupplierDto> findAll() {
        return materielSupplierRepository.findAllByIsDeletedFalse().stream()
                .map(s -> modelMapper.map(s, MaterielSupplierDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MaterielSupplierDto findById(UUID id) {
        MaterielSupplier supplier = materielSupplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material supplier not found with id: " + id));
        return modelMapper.map(supplier, MaterielSupplierDto.class);
    }

    @Override
    @Transactional
    public MaterielSupplierDto save(MaterielSupplierDto dto) {
        if (!StringUtils.hasText(dto.getNom())) {
            throw new RuntimeException("Material supplier name is required");
        }
        dto.setCode(generateSupplierCode());
        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            if (materielSupplierRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("A material supplier with this email already exists: " + dto.getEmail());
            }
        }
        if (dto.getTelephone() != null && !dto.getTelephone().isEmpty()) {
            if (materielSupplierRepository.existsByTelephone(dto.getTelephone())) {
                throw new RuntimeException("A material supplier with this phone already exists: " + dto.getTelephone());
            }
        }
        if (dto.getNumeroTva() != null && !dto.getNumeroTva().isEmpty()) {
            if (materielSupplierRepository.existsByNumeroTva(dto.getNumeroTva())) {
                throw new RuntimeException("A material supplier with this VAT number already exists: " + dto.getNumeroTva());
            }
        }
        MaterielSupplier supplier = modelMapper.map(dto, MaterielSupplier.class);

        if (supplier.getActif() == null) {
            supplier.setActif(true);
        }

        MaterielSupplier saved = materielSupplierRepository.save(supplier);
        return modelMapper.map(saved, MaterielSupplierDto.class);
    }

    @Override
    @Transactional
    public MaterielSupplierDto update(MaterielSupplierDto dto) {
        if (dto == null || dto.getId() == null) {
            throw new RuntimeException("Material supplier id is required");
        }
        UUID id = dto.getId();
        MaterielSupplier existing = materielSupplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material supplier not found with id: " + id));
        if (!StringUtils.hasText(dto.getNom())) {
            throw new RuntimeException("Material supplier name is required");
        }
        if (dto.getEmail() != null) {
            if (!dto.getEmail().equals(existing.getEmail())) {
                if (materielSupplierRepository.existsByEmail(dto.getEmail())) {
                    throw new RuntimeException("A material supplier with this email already exists: " + dto.getEmail());
                }
                existing.setEmail(dto.getEmail());
            }
        } else {
            existing.setEmail(null);
        }
        if (dto.getTelephone() != null) {
            if (!dto.getTelephone().equals(existing.getTelephone())) {
                if (materielSupplierRepository.existsByTelephone(dto.getTelephone())) {
                    throw new RuntimeException("A material supplier with this phone already exists: " + dto.getTelephone());
                }
                existing.setTelephone(dto.getTelephone());
            }
        } else {
            existing.setTelephone(null);
        }
        if (dto.getNumeroTva() != null) {
            if (!dto.getNumeroTva().equals(existing.getNumeroTva())) {
                if (materielSupplierRepository.existsByNumeroTva(dto.getNumeroTva())) {
                    throw new RuntimeException("A material supplier with this VAT number already exists: " + dto.getNumeroTva());
                }
                existing.setNumeroTva(dto.getNumeroTva());
            }
        } else {
            existing.setNumeroTva(null);
        }
        existing.setNom(dto.getNom());
        existing.setNomCommercial(dto.getNomCommercial());
        existing.setFax(dto.getFax());
        existing.setSiteWeb(dto.getSiteWeb());
        existing.setAdresse(dto.getAdresse());
        existing.setVille(dto.getVille());
        existing.setCodePostal(dto.getCodePostal());
        existing.setPays(dto.getPays());
        existing.setContactNom(dto.getContactNom());
        existing.setContactPrenom(dto.getContactPrenom());
        existing.setContactEmail(dto.getContactEmail());
        existing.setContactTelephone(dto.getContactTelephone());
        existing.setCategory(dto.getCategory());
        existing.setDelaiLivraisonMoyen(dto.getDelaiLivraisonMoyen());
        existing.setConditionsPaiement(dto.getConditionsPaiement());
        existing.setCurrency(dto.getCurrency());
        existing.setActif(dto.getActif());
        existing.setCertifications(dto.getCertifications());

        MaterielSupplier updated = materielSupplierRepository.save(existing);
        return modelMapper.map(updated, MaterielSupplierDto.class);
    }

    @Transactional
    public MaterielSupplierDto activate(UUID id) {
        MaterielSupplier supplier = materielSupplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material supplier not found with id: " + id));
        supplier.setActif(true);
        MaterielSupplier updated = materielSupplierRepository.save(supplier);
        return modelMapper.map(updated, MaterielSupplierDto.class);
    }

    @Transactional
    public MaterielSupplierDto deactivate(UUID id) {
        MaterielSupplier supplier = materielSupplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material supplier not found with id: " + id));
        deleteGuard.assertMaterielSupplierCanBeRemoved(id);
        supplier.setActif(false);
        MaterielSupplier updated = materielSupplierRepository.save(supplier);
        return modelMapper.map(updated, MaterielSupplierDto.class);
    }

    @Transactional
    public void removeSupplier(UUID id) {
        MaterielSupplier supplier = materielSupplierRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Material supplier not found with id: " + id));
        deleteGuard.assertMaterielSupplierCanBeRemoved(id);
        supplier.setDeleted(true);
        supplier.setActif(false);
        materielSupplierRepository.save(supplier);
    }

    @Override
    @Transactional
    public MaterielSupplierDto delete(UUID id) {
        MaterielSupplierDto dto = findById(id);
        removeSupplier(id);
        return dto;
    }

    @Override
    @Transactional
    public void remove(UUID id) {
        removeSupplier(id);
    }

    private String generateSupplierCode() {
        return generateBusinessCode("code", "MS");
    }

    @Transactional(readOnly = true)
    public List<MaterielSupplierDto> getActiveSuppliers() {
        return materielSupplierRepository.findByActifTrueAndIsDeletedFalse().stream()
                .map(s -> modelMapper.map(s, MaterielSupplierDto.class))
                .collect(Collectors.toList());
    }
}
