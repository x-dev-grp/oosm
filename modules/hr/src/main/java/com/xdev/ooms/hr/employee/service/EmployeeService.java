package com.xdev.ooms.hr.employee.service;

import com.xdev.ooms.hr.common.HrBusinessLinkageService;
import com.xdev.ooms.hr.common.HrRelationResolver;
import com.xdev.ooms.hr.common.enums.EmployeeStatus;
import com.xdev.ooms.hr.contract.dto.EmploymentContractDto;
import com.xdev.ooms.hr.contract.entity.EmploymentContract;
import com.xdev.ooms.hr.contract.repository.EmploymentContractRepository;
import com.xdev.ooms.hr.employee.dto.EmployeeDto;
import com.xdev.ooms.hr.employee.entity.Employee;
import com.xdev.ooms.hr.leave.dto.LeaveRequestDto;
import com.xdev.ooms.hr.leave.entity.LeaveRequest;
import com.xdev.ooms.hr.leave.repository.LeaveRequestRepository;
import com.xdev.ooms.hr.payslip.dto.PayslipDto;
import com.xdev.ooms.hr.payslip.entity.Payslip;
import com.xdev.ooms.hr.payslip.repository.PayslipRepository;
import com.xdev.ooms.hr.pointage.dto.PointageDto;
import com.xdev.ooms.hr.pointage.entity.Pointage;
import com.xdev.ooms.hr.pointage.repository.PointageRepository;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.AuditHelper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.xdev.ooms.hr.common.HrActionMappings.addRead;
import static com.xdev.ooms.hr.common.HrActionMappings.isActive;

@Service
public class EmployeeService extends BaseServiceImpl<Employee, EmployeeDto, EmployeeDto> {

    private final EmploymentContractRepository contractRepository;
    private final PointageRepository pointageRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PayslipRepository payslipRepository;
    private final HrBusinessLinkageService hrBusinessLinkage;
    private final HrRelationResolver hrRelationResolver;

    public EmployeeService(
            BaseRepository<Employee> repository,
            ModelMapper modelMapper,
            EmploymentContractRepository contractRepository,
            PointageRepository pointageRepository,
            LeaveRequestRepository leaveRequestRepository,
            PayslipRepository payslipRepository,
            HrBusinessLinkageService hrBusinessLinkage,
            HrRelationResolver hrRelationResolver
    ) {
        super(repository, modelMapper);
        this.contractRepository = contractRepository;
        this.pointageRepository = pointageRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.payslipRepository = payslipRepository;
        this.hrBusinessLinkage = hrBusinessLinkage;
        this.hrRelationResolver = hrRelationResolver;
    }

    @Override
    public void resolveEntityRelations(Employee entity) {
        entity.setGrade(hrRelationResolver.resolveGrade(entity.getGrade()));
        entity.setEmployeeCategory(hrRelationResolver.resolveEmployeeCategory(entity.getEmployeeCategory()));
        entity.setDepartmentRef(hrRelationResolver.resolveDepartment(entity.getDepartmentRef()));
    }

    @Override
    @Transactional
    public EmployeeDto save(EmployeeDto request) {
        stripChildCollections(request);
        Employee entity = modelMapper.map(request, entityClass);
        resolveEntityRelations(entity);
        hrBusinessLinkage.validateAndEnrichEmployee(entity, null);
        AuditHelper.applyAuditOnCreate(entity);
        Employee saved = repository.save(entity);
        return modelMapper.map(saved, outDTOClass);
    }

    @Override
    @Transactional
    public EmployeeDto update(EmployeeDto request) {
        if (request == null || request.getId() == null) {
            return null;
        }
        stripChildCollections(request);
        Employee existing = repository.findById(request.getId()).orElse(null);
        if (existing == null) {
            return null;
        }
        AuditHelper.applyAuditOnCreate(existing);
        modelMapper.map(request, existing);
        resolveEntityRelations(existing);
        hrBusinessLinkage.validateAndEnrichEmployee(existing, existing.getId());
        Employee updated = repository.save(existing);
        return modelMapper.map(updated, outDTOClass);
    }

    private void stripChildCollections(EmployeeDto request) {
        if (request == null) {
            return;
        }
        request.setContracts(null);
        request.setActiveContract(null);
        request.setPointages(null);
        request.setLeaveRequests(null);
        request.setPayslips(null);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDto findById(UUID id) {
        EmployeeDto dto = super.findById(id);
        dto.setActiveContract(mapActiveContract(id));
        dto.setContracts(mapContracts(contractRepository.findByEmployee_IdAndIsDeletedFalse(id)));
        dto.setPointages(mapPointages(pointageRepository.findByEmployee_IdAndIsDeletedFalse(id)));
        dto.setLeaveRequests(mapLeaveRequests(leaveRequestRepository.findByEmployee_IdAndIsDeletedFalse(id)));
        dto.setPayslips(mapPayslips(payslipRepository.findByEmployee_IdAndIsDeletedFalse(id)));
        return dto;
    }

    @Override
    public Set<Action> actionsMapping(Employee employee) {
        Set<Action> actions = new HashSet<>();
        addRead(actions);
        if (!isActive(employee)) {
            return actions;
        }
        if (employee.getStatus() != EmployeeStatus.TERMINATED
                && employee.getStatus() != EmployeeStatus.RETIRED) {
            actions.add(Action.UPDATE);
            actions.add(Action.DELETE);
        }
        actions.add(Action.EXPORT);
        actions.add(Action.GEN_PDF);
        return actions;
    }

    private EmploymentContractDto mapActiveContract(UUID employeeId) {
        return hrBusinessLinkage.findActiveContractOnDate(employeeId, LocalDate.now())
                .map(contract -> {
                    EmploymentContractDto contractDto = modelMapper.map(contract, EmploymentContractDto.class);
                    contractDto.setEmployee(null);
                    return contractDto;
                })
                .orElse(null);
    }

    private List<EmploymentContractDto> mapContracts(List<EmploymentContract> contracts) {
        return contracts.stream().map(contract -> {
            EmploymentContractDto dto = modelMapper.map(contract, EmploymentContractDto.class);
            dto.setEmployee(null);
            return dto;
        }).toList();
    }

    private List<PointageDto> mapPointages(List<Pointage> pointages) {
        return pointages.stream().map(pointage -> {
            PointageDto dto = modelMapper.map(pointage, PointageDto.class);
            dto.setEmployee(null);
            return dto;
        }).toList();
    }

    private List<LeaveRequestDto> mapLeaveRequests(List<LeaveRequest> leaveRequests) {
        return leaveRequests.stream().map(leaveRequest -> {
            LeaveRequestDto dto = modelMapper.map(leaveRequest, LeaveRequestDto.class);
            dto.setEmployee(null);
            return dto;
        }).toList();
    }

    private List<PayslipDto> mapPayslips(List<Payslip> payslips) {
        return payslips.stream().map(payslip -> {
            PayslipDto dto = modelMapper.map(payslip, PayslipDto.class);
            dto.setEmployee(null);
            return dto;
        }).toList();
    }
}
