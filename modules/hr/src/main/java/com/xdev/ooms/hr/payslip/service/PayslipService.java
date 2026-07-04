package com.xdev.ooms.hr.payslip.service;

import com.xdev.ooms.hr.common.HrBusinessLinkageService;
import com.xdev.ooms.hr.common.HrRelationResolver;
import com.xdev.ooms.hr.common.enums.PayrollPeriodStatus;
import com.xdev.ooms.hr.common.enums.PayslipStatus;
import com.xdev.ooms.hr.payslip.dto.PayslipDto;
import com.xdev.ooms.hr.payslip.entity.Payslip;
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
public class PayslipService extends BaseServiceImpl<Payslip, PayslipDto, PayslipDto> {

    private final HrRelationResolver hrRelationResolver;
    private final HrBusinessLinkageService hrBusinessLinkage;

    public PayslipService(
            BaseRepository<Payslip> repository,
            ModelMapper modelMapper,
            HrRelationResolver hrRelationResolver,
            HrBusinessLinkageService hrBusinessLinkage
    ) {
        super(repository, modelMapper);
        this.hrRelationResolver = hrRelationResolver;
        this.hrBusinessLinkage = hrBusinessLinkage;
    }

    @Override
    public void resolveEntityRelations(Payslip entity) {
        entity.setEmployee(hrRelationResolver.resolveEmployee(entity.getEmployee()));
        entity.setPayrollPeriod(hrRelationResolver.resolvePayrollPeriod(entity.getPayrollPeriod()));
    }

    @Override
    @Transactional
    public PayslipDto save(PayslipDto request) {
        Payslip entity = modelMapper.map(request, entityClass);
        resolveEntityRelations(entity);
        hrBusinessLinkage.validateAndEnrichPayslip(entity, null);
        AuditHelper.applyAuditOnCreate(entity);
        Payslip saved = repository.save(entity);
        return modelMapper.map(saved, outDTOClass);
    }

    @Override
    @Transactional
    public PayslipDto update(PayslipDto request) {
        if (request == null || request.getId() == null) {
            return null;
        }
        Payslip existing = repository.findById(request.getId()).orElse(null);
        if (existing == null) {
            return null;
        }
        AuditHelper.applyAuditOnCreate(existing);
        modelMapper.map(request, existing);
        resolveEntityRelations(existing);
        hrBusinessLinkage.validateAndEnrichPayslip(existing, existing.getId());
        Payslip updated = repository.save(existing);
        return modelMapper.map(updated, outDTOClass);
    }

    @Override
    public Set<Action> actionsMapping(Payslip payslip) {
        Set<Action> actions = new HashSet<>();
        addRead(actions);
        if (!isActive(payslip)) {
            return actions;
        }
        if (isPeriodLocked(payslip)) {
            actions.add(Action.GEN_PDF);
            actions.add(Action.EXPORT);
            return actions;
        }
        PayslipStatus status = payslip.getStatus() != null ? payslip.getStatus() : PayslipStatus.DRAFT;
        actions.add(Action.UPDATE);
        actions.add(Action.DELETE);
        actions.add(Action.CALCULATE);
        actions.add(Action.GEN_PDF);
        actions.add(Action.EXPORT);
        if (status == PayslipStatus.DRAFT) {
            actions.add(Action.VALIDATE);
        } else if (status == PayslipStatus.VALIDATED) {
            actions.add(Action.PAY);
        }
        return actions;
    }

    private boolean isPeriodLocked(Payslip payslip) {
        if (payslip.getPayrollPeriod() == null || payslip.getPayrollPeriod().getStatus() == null) {
            return false;
        }
        PayrollPeriodStatus periodStatus = payslip.getPayrollPeriod().getStatus();
        return periodStatus == PayrollPeriodStatus.PAID || periodStatus == PayrollPeriodStatus.CLOSED;
    }
}
