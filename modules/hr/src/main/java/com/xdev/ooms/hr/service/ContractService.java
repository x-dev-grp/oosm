package com.xdev.ooms.hr.service;

import com.xdev.ooms.hr.Dtos.ContractDto;
import com.xdev.ooms.hr.model.Contract;
import com.xdev.ooms.hr.model.Department;
import com.xdev.ooms.hr.repo.EmployeeRepository;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ContractService extends BaseServiceImpl<Contract, ContractDto, ContractDto> {
    private final EmployeeRepository employeeRepository;
    public ContractService(BaseRepository<Contract> repository, ModelMapper modelMapper, EmployeeRepository employeeRepository) {
        super(repository, modelMapper);
        this.employeeRepository = employeeRepository;
    }
    @Override
    public Set<Action> actionsMapping(Contract Contract) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "actionsMapping", Contract);
        Set<Action> actions = new HashSet<>();
        actions.add(Action.READ);
        actions.add(Action.UPDATE);
        actions.add(Action.DELETE);
        actions.add(Action.PAY);
        OSMLogger.logMethodExit(this.getClass(), "actionsMapping", actions);
        OSMLogger.logPerformance(this.getClass(), "actionsMapping", startTime, System.currentTimeMillis());
        return actions;



}
    // Ajouter un contrat a un employé
    public ContractDto addContractToEmployee(UUID employeeId, ContractDto contractDto) {
        // Fetch the employee
        var employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Map DTO to entity
        Contract contract = modelMapper.map(contractDto, Contract.class);

        // Set the bidirectional relationship
        contract.setEmployee(employee);

        // Add the contract to the employee's contract list
        employee.getContrats().add(contract);

        // Save contract (or employee, depending on cascade)
        Contract savedContract = repository.save(contract);

        // Return DTO
        return modelMapper.map(savedContract, ContractDto.class);
    }

    public List<ContractDto> getContractsByEmployee(UUID employeeId) {
        var employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return employee.getContrats()
                .stream()
                .map(c -> modelMapper.map(c, ContractDto.class))
                .toList();
    }

    public ContractDto updateEmployeeContract(UUID employeeId, UUID contractId, ContractDto contractDto) {
        var contract = repository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
        if (!contract.getEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("Contract does not belong to this employee");
        }
        modelMapper.map(contractDto, contract);
        Contract updatedContract = repository.save(contract);
        return modelMapper.map(updatedContract, ContractDto.class);



    }

    //delete contract
    public void deleteEmployeeContract(UUID employeeId, UUID contractId) {
        var contract = repository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        // Vérifier que le contrat appartient à l'employé
        if (!contract.getEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("Contract does not belong to this employee");
        }

        // Supprimer le contrat
        repository.delete(contract);
    }























}