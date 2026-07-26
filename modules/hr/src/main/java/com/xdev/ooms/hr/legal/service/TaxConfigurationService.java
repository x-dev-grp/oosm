package com.xdev.ooms.hr.legal.service;

import com.xdev.ooms.hr.legal.dto.TaxBracketDto;
import com.xdev.ooms.hr.legal.dto.TaxConfigurationDto;
import com.xdev.ooms.hr.legal.entity.TaxConfiguration;
import com.xdev.ooms.hr.legal.repository.TaxBracketRepository;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.AuditHelper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.xdev.ooms.hr.common.HrActionMappings.addCrudIfActive;
import static com.xdev.ooms.hr.common.HrActionMappings.isActive;

@Service
public class TaxConfigurationService extends BaseServiceImpl<TaxConfiguration, TaxConfigurationDto, TaxConfigurationDto> {

    private final TaxBracketRepository taxBracketRepository;

    public TaxConfigurationService(
            BaseRepository<TaxConfiguration> repository,
            ModelMapper modelMapper,
            TaxBracketRepository taxBracketRepository
    ) {
        super(repository, modelMapper);
        this.taxBracketRepository = taxBracketRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public TaxConfigurationDto findById(UUID id) {
        TaxConfiguration entity = repository.findById(id).orElse(null);
        if (entity == null) {
            return null;
        }
        entity.setBrackets(null);
        TaxConfigurationDto dto = modelMapper.map(entity, outDTOClass);
        dto.setBrackets(mapBrackets(id));
        return dto;
    }

    @Override
    @Transactional
    public TaxConfigurationDto save(TaxConfigurationDto request) {
        stripBrackets(request);
        TaxConfiguration entity = modelMapper.map(request, entityClass);
        entity.setBrackets(null);
        AuditHelper.applyAuditOnCreate(entity);
        TaxConfiguration saved = repository.save(entity);
        TaxConfigurationDto dto = modelMapper.map(saved, outDTOClass);
        dto.setBrackets(List.of());
        return dto;
    }

    @Override
    @Transactional
    public TaxConfigurationDto update(TaxConfigurationDto request) {
        if (request == null || request.getId() == null) {
            return null;
        }
        stripBrackets(request);
        TaxConfiguration existing = repository.findById(request.getId()).orElse(null);
        if (existing == null) {
            return null;
        }
        existing.setBrackets(null);
        AuditHelper.applyAuditOnCreate(existing);
        modelMapper.map(request, existing);
        existing.setBrackets(null);
        TaxConfiguration updated = repository.save(existing);
        TaxConfigurationDto dto = modelMapper.map(updated, outDTOClass);
        dto.setBrackets(mapBrackets(updated.getId()));
        return dto;
    }

    private void stripBrackets(TaxConfigurationDto request) {
        if (request != null) {
            request.setBrackets(null);
        }
    }

    private List<TaxBracketDto> mapBrackets(UUID taxConfigurationId) {
        return taxBracketRepository.findByTaxConfiguration_IdAndIsDeletedFalseOrderBySortOrderAsc(taxConfigurationId)
                .stream()
                .map(bracket -> {
                    TaxBracketDto bracketDto = modelMapper.map(bracket, TaxBracketDto.class);
                    bracketDto.setTaxConfiguration(null);
                    return bracketDto;
                })
                .toList();
    }

    @Override
    public Set<Action> actionsMapping(TaxConfiguration entity) {
        Set<Action> actions = new HashSet<>();
        addCrudIfActive(actions, entity);
        if (isActive(entity)) {
            actions.add(Action.EXPORT);
        }
        return actions;
    }
}
