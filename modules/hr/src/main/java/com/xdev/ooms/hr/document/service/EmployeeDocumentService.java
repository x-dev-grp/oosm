package com.xdev.ooms.hr.document.service;

import com.xdev.ooms.hr.common.HrRelationResolver;
import com.xdev.ooms.hr.document.dto.EmployeeDocumentDto;
import com.xdev.ooms.hr.document.entity.EmployeeDocument;
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
public class EmployeeDocumentService extends BaseServiceImpl<EmployeeDocument, EmployeeDocumentDto, EmployeeDocumentDto> {

    private final HrRelationResolver hrRelationResolver;

    public EmployeeDocumentService(
            BaseRepository<EmployeeDocument> repository,
            ModelMapper modelMapper,
            HrRelationResolver hrRelationResolver
    ) {
        super(repository, modelMapper);
        this.hrRelationResolver = hrRelationResolver;
    }

    @Override
    public void resolveEntityRelations(EmployeeDocument entity) {
        entity.setEmployee(hrRelationResolver.resolveEmployee(entity.getEmployee()));
    }

    @Override
    public Set<Action> actionsMapping(EmployeeDocument document) {
        Set<Action> actions = new HashSet<>();
        addCrudIfActive(actions, document);
        if (isActive(document)) {
            actions.add(Action.EXPORT);
        }
        return actions;
    }
}
