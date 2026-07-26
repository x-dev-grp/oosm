package com.xdev.ooms.hr.legal.service;

import com.xdev.ooms.hr.legal.dto.TaxBracketDto;
import com.xdev.ooms.hr.legal.entity.TaxBracket;
import com.xdev.ooms.hr.legal.entity.TaxConfiguration;
import com.xdev.ooms.hr.legal.repository.TaxConfigurationRepository;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.AuditHelper;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static com.xdev.ooms.hr.common.HrActionMappings.addCrudIfActive;
import static com.xdev.ooms.hr.common.HrActionMappings.isActive;

@Service
public class TaxBracketService extends BaseServiceImpl<TaxBracket, TaxBracketDto, TaxBracketDto> {

    private final TaxConfigurationRepository taxConfigurationRepository;

    public TaxBracketService(
            BaseRepository<TaxBracket> repository,
            ModelMapper modelMapper,
            TaxConfigurationRepository taxConfigurationRepository
    ) {
        super(repository, modelMapper);
        this.taxConfigurationRepository = taxConfigurationRepository;
    }

    @Override
    public void resolveEntityRelations(TaxBracket entity) {
        entity.setTaxConfiguration(resolveTaxConfiguration(entity.getTaxConfiguration()));
    }

    private TaxConfiguration resolveTaxConfiguration(TaxConfiguration taxConfiguration) {
        if (taxConfiguration == null || taxConfiguration.getId() == null) {
            return null;
        }
        return taxConfigurationRepository.findByIdAndIsDeletedFalse(taxConfiguration.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "TaxConfiguration not found with id: " + taxConfiguration.getId()));
    }

    @Override
    @Transactional
    public TaxBracketDto save(TaxBracketDto request) {
        TaxBracket entity = modelMapper.map(request, entityClass);
        resolveEntityRelations(entity);
        AuditHelper.applyAuditOnCreate(entity);
        TaxBracket saved = repository.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional
    public TaxBracketDto update(TaxBracketDto request) {
        if (request == null || request.getId() == null) {
            return null;
        }
        TaxBracket existing = repository.findById(request.getId()).orElse(null);
        if (existing == null) {
            return null;
        }
        AuditHelper.applyAuditOnCreate(existing);
        modelMapper.map(request, existing);
        resolveEntityRelations(existing);
        TaxBracket updated = repository.save(existing);
        return toDto(updated);
    }

    private TaxBracketDto toDto(TaxBracket entity) {
        TaxBracketDto dto = modelMapper.map(entity, outDTOClass);
        if (dto.getTaxConfiguration() != null) {
            dto.getTaxConfiguration().setBrackets(null);
        }
        return dto;
    }

    @Override
    public Set<Action> actionsMapping(TaxBracket entity) {
        Set<Action> actions = new HashSet<>();
        addCrudIfActive(actions, entity);
        if (isActive(entity)) {
            actions.add(Action.EXPORT);
        }
        return actions;
    }
}
