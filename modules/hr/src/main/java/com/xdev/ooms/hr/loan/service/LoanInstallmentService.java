package com.xdev.ooms.hr.loan.service;

import com.xdev.ooms.hr.common.HrRelationResolver;
import com.xdev.ooms.hr.common.enums.LoanInstallmentStatus;
import com.xdev.ooms.hr.loan.dto.LoanInstallmentDto;
import com.xdev.ooms.hr.loan.entity.LoanInstallment;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.AuditHelper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static com.xdev.ooms.hr.common.HrActionMappings.addCrudIfActive;

@Service
public class LoanInstallmentService extends BaseServiceImpl<LoanInstallment, LoanInstallmentDto, LoanInstallmentDto> {

    private final HrRelationResolver hrRelationResolver;

    public LoanInstallmentService(
            BaseRepository<LoanInstallment> repository,
            ModelMapper modelMapper,
            HrRelationResolver hrRelationResolver
    ) {
        super(repository, modelMapper);
        this.hrRelationResolver = hrRelationResolver;
    }

    @Override
    public void resolveEntityRelations(LoanInstallment entity) {
        entity.setLoan(hrRelationResolver.resolveEmployeeLoan(entity.getLoan()));
    }

    @Override
    @Transactional
    public LoanInstallmentDto save(LoanInstallmentDto request) {
        LoanInstallment entity = modelMapper.map(request, entityClass);
        if (entity.getStatus() == null) {
            entity.setStatus(LoanInstallmentStatus.PENDING);
        }
        resolveEntityRelations(entity);
        AuditHelper.applyAuditOnCreate(entity);
        LoanInstallment saved = repository.save(entity);
        return modelMapper.map(saved, outDTOClass);
    }

    @Override
    public Set<Action> actionsMapping(LoanInstallment entity) {
        Set<Action> actions = new HashSet<>();
        addCrudIfActive(actions, entity);
        return actions;
    }
}
