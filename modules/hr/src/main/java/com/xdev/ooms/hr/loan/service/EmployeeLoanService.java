package com.xdev.ooms.hr.loan.service;

import com.xdev.ooms.hr.common.HrRelationResolver;
import com.xdev.ooms.hr.common.enums.EmployeeLoanStatus;
import com.xdev.ooms.hr.common.enums.LoanInstallmentStatus;
import com.xdev.ooms.hr.loan.dto.EmployeeLoanDto;
import com.xdev.ooms.hr.loan.dto.LoanInstallmentDto;
import com.xdev.ooms.hr.loan.entity.EmployeeLoan;
import com.xdev.ooms.hr.loan.entity.LoanInstallment;
import com.xdev.ooms.hr.loan.repository.LoanInstallmentRepository;
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
public class EmployeeLoanService extends BaseServiceImpl<EmployeeLoan, EmployeeLoanDto, EmployeeLoanDto> {

    private final HrRelationResolver hrRelationResolver;
    private final LoanInstallmentRepository loanInstallmentRepository;

    public EmployeeLoanService(
            BaseRepository<EmployeeLoan> repository,
            ModelMapper modelMapper,
            HrRelationResolver hrRelationResolver,
            LoanInstallmentRepository loanInstallmentRepository
    ) {
        super(repository, modelMapper);
        this.hrRelationResolver = hrRelationResolver;
        this.loanInstallmentRepository = loanInstallmentRepository;
    }

    @Override
    public void resolveEntityRelations(EmployeeLoan entity) {
        entity.setEmployee(hrRelationResolver.resolveEmployee(entity.getEmployee()));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeLoanDto findById(UUID id) {
        EmployeeLoan entity = repository.findById(id).orElse(null);
        if (entity == null) {
            return null;
        }
        entity.setInstallments(null);
        EmployeeLoanDto dto = modelMapper.map(entity, outDTOClass);
        dto.setInstallments(mapInstallments(id));
        return dto;
    }

    @Override
    @Transactional
    public EmployeeLoanDto save(EmployeeLoanDto request) {
        List<LoanInstallmentDto> incoming = request != null ? request.getInstallments() : null;
        if (request != null) {
            request.setInstallments(null);
        }
        EmployeeLoan entity = modelMapper.map(request, entityClass);
        entity.setInstallments(null);
        if (entity.getStatus() == null) {
            entity.setStatus(EmployeeLoanStatus.DRAFT);
        }
        if (entity.getRemainingBalance() == null) {
            entity.setRemainingBalance(entity.getPrincipalAmount());
        }
        resolveEntityRelations(entity);
        AuditHelper.applyAuditOnCreate(entity);
        EmployeeLoan saved = repository.save(entity);
        persistInstallments(saved, incoming);
        EmployeeLoanDto dto = modelMapper.map(saved, outDTOClass);
        dto.setInstallments(mapInstallments(saved.getId()));
        return dto;
    }

    @Override
    @Transactional
    public EmployeeLoanDto update(EmployeeLoanDto request) {
        if (request == null || request.getId() == null) {
            return null;
        }
        List<LoanInstallmentDto> incoming = request.getInstallments();
        request.setInstallments(null);
        EmployeeLoan existing = repository.findById(request.getId()).orElse(null);
        if (existing == null) {
            return null;
        }
        existing.setInstallments(null);
        AuditHelper.applyAuditOnCreate(existing);
        modelMapper.map(request, existing);
        existing.setInstallments(null);
        resolveEntityRelations(existing);
        EmployeeLoan updated = repository.save(existing);
        if (incoming != null) {
            persistInstallments(updated, incoming);
        }
        EmployeeLoanDto dto = modelMapper.map(updated, outDTOClass);
        dto.setInstallments(mapInstallments(updated.getId()));
        return dto;
    }

    private void persistInstallments(EmployeeLoan loan, List<LoanInstallmentDto> installmentDtos) {
        if (installmentDtos == null || installmentDtos.isEmpty()) {
            return;
        }
        for (LoanInstallmentDto installmentDto : installmentDtos) {
            LoanInstallment installment = modelMapper.map(installmentDto, LoanInstallment.class);
            installment.setLoan(loan);
            if (installment.getStatus() == null) {
                installment.setStatus(LoanInstallmentStatus.PENDING);
            }
            AuditHelper.applyAuditOnCreate(installment);
            loanInstallmentRepository.save(installment);
        }
    }

    private List<LoanInstallmentDto> mapInstallments(UUID loanId) {
        return loanInstallmentRepository.findByLoan_IdAndIsDeletedFalseOrderByDueDateAsc(loanId)
                .stream()
                .map(installment -> {
                    LoanInstallmentDto dto = modelMapper.map(installment, LoanInstallmentDto.class);
                    dto.setLoan(null);
                    return dto;
                })
                .toList();
    }

    @Override
    public Set<Action> actionsMapping(EmployeeLoan entity) {
        Set<Action> actions = new HashSet<>();
        addCrudIfActive(actions, entity);
        if (isActive(entity)) {
            actions.add(Action.EXPORT);
        }
        return actions;
    }
}
