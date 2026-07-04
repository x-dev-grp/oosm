package com.xdev.ooms.hr.payroll.service;

import com.xdev.ooms.hr.common.HrBusinessLinkageService;
import com.xdev.ooms.hr.common.HrPermissionSupport;
import com.xdev.ooms.hr.common.enums.PayrollPeriodStatus;
import com.xdev.ooms.hr.contract.repository.EmploymentContractRepository;
import com.xdev.ooms.hr.employee.entity.Employee;
import com.xdev.ooms.hr.employee.repository.EmployeeRepository;
import com.xdev.ooms.hr.employee.dto.EmployeeDto;
import com.xdev.ooms.hr.payroll.dto.PayrollPeriodDto;
import com.xdev.ooms.hr.payroll.entity.PayrollPeriod;
import com.xdev.ooms.hr.payslip.dto.PayslipDto;
import com.xdev.ooms.hr.payslip.entity.Payslip;
import com.xdev.ooms.hr.payslip.repository.PayslipRepository;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.AuditHelper;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.xdev.ooms.hr.common.HrActionMappings.addRead;
import static com.xdev.ooms.hr.common.HrActionMappings.isActive;

@Service
public class PayrollPeriodService extends BaseServiceImpl<PayrollPeriod, PayrollPeriodDto, PayrollPeriodDto> {

    private static final EnumSet<PayrollPeriodStatus> LOCKED_STATUSES = EnumSet.of(
            PayrollPeriodStatus.PAID,
            PayrollPeriodStatus.CLOSED
    );

    private final HrBusinessLinkageService hrBusinessLinkage;
    private final PayslipRepository payslipRepository;
    private final EmploymentContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;

    public PayrollPeriodService(
            BaseRepository<PayrollPeriod> repository,
            ModelMapper modelMapper,
            HrBusinessLinkageService hrBusinessLinkage,
            PayslipRepository payslipRepository,
            EmploymentContractRepository contractRepository,
            EmployeeRepository employeeRepository
    ) {
        super(repository, modelMapper);
        this.hrBusinessLinkage = hrBusinessLinkage;
        this.payslipRepository = payslipRepository;
        this.contractRepository = contractRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollPeriodDto findById(UUID id) {
        PayrollPeriodDto dto = super.findById(id);
        dto.setPayslips(mapPayslips(payslipRepository.findByPayrollPeriod_IdAndIsDeletedFalse(id)));
        return dto;
    }

    @Override
    @Transactional
    public PayrollPeriodDto save(PayrollPeriodDto request) {
        PayrollPeriod entity = modelMapper.map(request, entityClass);
        hrBusinessLinkage.validateAndEnrichPayrollPeriod(entity, null);
        AuditHelper.applyAuditOnCreate(entity);
        PayrollPeriod saved = repository.save(entity);
        return modelMapper.map(saved, outDTOClass);
    }

    @Override
    @Transactional
    public PayrollPeriodDto update(PayrollPeriodDto request) {
        if (request == null || request.getId() == null) {
            return null;
        }
        PayrollPeriod existing = repository.findById(request.getId()).orElse(null);
        if (existing == null) {
            return null;
        }
        if (LOCKED_STATUSES.contains(existing.getStatus())) {
            throw new IllegalArgumentException("Closed payroll periods cannot be modified");
        }
        PayrollPeriodStatus previousStatus = existing.getStatus();
        AuditHelper.applyAuditOnCreate(existing);
        request.setPayslips(null);
        modelMapper.map(request, existing);
        if (existing.getStatus() != previousStatus) {
            hrBusinessLinkage.validatePayrollPeriodStatusTransition(previousStatus, existing.getStatus());
        }
        hrBusinessLinkage.validateAndEnrichPayrollPeriod(existing, existing.getId());
        PayrollPeriod updated = repository.save(existing);
        return modelMapper.map(updated, outDTOClass);
    }

    @Transactional
    public PayrollPeriodDto generatePayslips(UUID periodId) {
        HrPermissionSupport.requireAction("PAYROLLPERIOD", Action.CALCULATE);
        PayrollPeriod period = repository.findByIdAndIsDeletedFalse(periodId)
                .orElseThrow(() -> new EntityNotFoundException("Payroll period not found: " + periodId));
        if (LOCKED_STATUSES.contains(period.getStatus())) {
            throw new IllegalArgumentException("Payroll period is closed; payslips cannot be generated");
        }
        if (period.getPeriodStart() == null || period.getPeriodEnd() == null) {
            throw new IllegalArgumentException("Payroll period dates are required before generating payslips");
        }

        List<UUID> employeeIds = contractRepository.findEmployeeIdsWithActiveContractInPeriod(
                period.getPeriodStart(), period.getPeriodEnd());

        for (UUID employeeId : employeeIds) {
            if (payslipRepository.existsByEmployee_IdAndPayrollPeriod_IdAndIdNotAndIsDeletedFalse(
                    employeeId, periodId, null)) {
                continue;
            }
            Employee employee = employeeRepository.findByIdAndIsDeletedFalse(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + employeeId));
            Payslip payslip = new Payslip();
            payslip.setEmployee(employee);
            payslip.setPayrollPeriod(period);
            hrBusinessLinkage.validateAndEnrichPayslip(payslip, null);
            AuditHelper.applyAuditOnCreate(payslip);
            payslipRepository.save(payslip);
        }

        if (period.getStatus() == PayrollPeriodStatus.OPEN) {
            period.setStatus(PayrollPeriodStatus.CALCULATED);
            repository.save(period);
        }
        return findById(periodId);
    }

    @Transactional
    public PayrollPeriodDto advanceStatus(UUID periodId, PayrollPeriodStatus targetStatus) {
        requireAdvancePermission(targetStatus);
        PayrollPeriod period = repository.findByIdAndIsDeletedFalse(periodId)
                .orElseThrow(() -> new EntityNotFoundException("Payroll period not found: " + periodId));
        PayrollPeriodStatus current = period.getStatus() != null ? period.getStatus() : PayrollPeriodStatus.OPEN;
        hrBusinessLinkage.validatePayrollPeriodStatusTransition(current, targetStatus);
        period.setStatus(targetStatus);
        AuditHelper.applyAuditOnCreate(period);
        repository.save(period);
        return findById(periodId);
    }

    @Override
    public Set<Action> actionsMapping(PayrollPeriod period) {
        Set<Action> actions = new HashSet<>();
        addRead(actions);
        if (!isActive(period)) {
            return actions;
        }
        PayrollPeriodStatus status = period.getStatus() != null ? period.getStatus() : PayrollPeriodStatus.OPEN;
        actions.add(Action.EXPORT);
        actions.add(Action.REPORT);
        switch (status) {
            case OPEN -> {
                actions.add(Action.UPDATE);
                actions.add(Action.DELETE);
                actions.add(Action.CALCULATE);
            }
            case CALCULATED -> {
                actions.add(Action.UPDATE);
                actions.add(Action.CALCULATE);
                actions.add(Action.VALIDATE);
            }
            case VALIDATED -> actions.add(Action.PAY);
            case PAID -> actions.add(Action.CLOSE);
            case CLOSED -> {
            }
        }
        return actions;
    }

    private void requireAdvancePermission(PayrollPeriodStatus targetStatus) {
        Action required = switch (targetStatus) {
            case CALCULATED -> Action.CALCULATE;
            case VALIDATED -> Action.VALIDATE;
            case PAID -> Action.PAY;
            case CLOSED -> Action.CLOSE;
            default -> Action.UPDATE;
        };
        HrPermissionSupport.requireAction("PAYROLLPERIOD", required);
    }

    private List<PayslipDto> mapPayslips(List<Payslip> payslips) {
        return payslips.stream().map(payslip -> {
            PayslipDto dto = modelMapper.map(payslip, PayslipDto.class);
            if (payslip.getEmployee() != null) {
                EmployeeDto employeeDto = new EmployeeDto();
                employeeDto.setId(payslip.getEmployee().getId());
                employeeDto.setFirstName(payslip.getEmployee().getFirstName());
                employeeDto.setLastName(payslip.getEmployee().getLastName());
                dto.setEmployee(employeeDto);
            }
            dto.setPayrollPeriod(null);
            return dto;
        }).toList();
    }
}
