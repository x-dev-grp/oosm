package com.xdev.ooms.hr.contract.service;

import com.xdev.ooms.hr.common.HrRelationResolver;
import com.xdev.ooms.hr.contract.dto.ContractAmendmentDto;
import com.xdev.ooms.hr.contract.entity.ContractAmendment;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

import static com.xdev.ooms.hr.common.HrActionMappings.addCrudIfActive;
import static com.xdev.ooms.hr.common.HrActionMappings.isActive;

@Service
public class ContractAmendmentService extends BaseServiceImpl<ContractAmendment, ContractAmendmentDto, ContractAmendmentDto> {

    private final HrRelationResolver hrRelationResolver;

    public ContractAmendmentService(
            BaseRepository<ContractAmendment> repository,
            ModelMapper modelMapper,
            HrRelationResolver hrRelationResolver
    ) {
        super(repository, modelMapper);
        this.hrRelationResolver = hrRelationResolver;
    }

    @Override
    public void resolveEntityRelations(ContractAmendment entity) {
        entity.setContract(hrRelationResolver.resolveEmploymentContract(entity.getContract()));
    }

    @Override
    public Set<Action> actionsMapping(ContractAmendment amendment) {
        Set<Action> actions = new HashSet<>();
        addCrudIfActive(actions, amendment);
        if (isActive(amendment)) {
            actions.add(Action.EXPORT);
        }
        return actions;
    }
}
