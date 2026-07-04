package com.xdev.ooms.hr.contract.service;

import com.xdev.ooms.hr.common.HrBusinessLinkageService;
import com.xdev.ooms.hr.common.HrRelationResolver;
import com.xdev.ooms.hr.common.enums.ContractStatus;
import com.xdev.ooms.hr.contract.dto.EmploymentContractDto;
import com.xdev.ooms.hr.contract.entity.EmploymentContract;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.AuditHelper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static com.xdev.ooms.hr.common.HrActionMappings.addRead;
import static com.xdev.ooms.hr.common.HrActionMappings.isActive;

@Service
public class EmploymentContractService extends BaseServiceImpl<EmploymentContract, EmploymentContractDto, EmploymentContractDto> {

    private final HrRelationResolver hrRelationResolver;
    private final HrBusinessLinkageService hrBusinessLinkage;

    public EmploymentContractService(
            BaseRepository<EmploymentContract> repository,
            ModelMapper modelMapper,
            HrRelationResolver hrRelationResolver,
            HrBusinessLinkageService hrBusinessLinkage
    ) {
        super(repository, modelMapper);
        this.hrRelationResolver = hrRelationResolver;
        this.hrBusinessLinkage = hrBusinessLinkage;
    }

    @Override
    public void resolveEntityRelations(EmploymentContract entity) {
        entity.setEmployee(hrRelationResolver.resolveEmployee(entity.getEmployee()));
        entity.setPoste(hrRelationResolver.resolvePoste(entity.getPoste()));
    }

    @Override
    @Transactional
    public EmploymentContractDto save(EmploymentContractDto request) {
        EmploymentContract entity = modelMapper.map(request, entityClass);
        resolveEntityRelations(entity);
        hrBusinessLinkage.validateAndEnrichContract(entity, null);
        AuditHelper.applyAuditOnCreate(entity);
        EmploymentContract saved = repository.save(entity);
        hrBusinessLinkage.syncEmployeeFromActiveContract(saved);
        return modelMapper.map(saved, outDTOClass);
    }

    @Override
    @Transactional
    public EmploymentContractDto update(EmploymentContractDto request) {
        if (request == null || request.getId() == null) {
            return null;
        }
        EmploymentContract existing = repository.findById(request.getId()).orElse(null);
        if (existing == null) {
            return null;
        }
        AuditHelper.applyAuditOnCreate(existing);
        modelMapper.map(request, existing);
        resolveEntityRelations(existing);
        hrBusinessLinkage.validateAndEnrichContract(existing, existing.getId());
        EmploymentContract updated = repository.save(existing);
        hrBusinessLinkage.syncEmployeeFromActiveContract(updated);
        return modelMapper.map(updated, outDTOClass);
    }

    @Override
    public Set<Action> actionsMapping(EmploymentContract contract) {
        Set<Action> actions = new HashSet<>();
        addRead(actions);
        if (!isActive(contract)) {
            return actions;
        }
        ContractStatus status = contract.getStatus() != null ? contract.getStatus() : ContractStatus.DRAFT;
        if (status == ContractStatus.DRAFT) {
            actions.add(Action.UPDATE);
            actions.add(Action.DELETE);
            actions.add(Action.UPDATE_STATUS);
        } else if (status == ContractStatus.ACTIVE) {
            actions.add(Action.UPDATE);
            actions.add(Action.UPDATE_STATUS);
            actions.add(Action.GEN_PDF);
        } else {
            actions.add(Action.GEN_PDF);
        }
        return actions;
    }
}
