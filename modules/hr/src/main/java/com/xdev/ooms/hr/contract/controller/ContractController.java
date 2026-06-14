package com.xdev.ooms.hr.contract.controller;

import com.xdev.ooms.hr.contract.dto.ContractDto;
import com.xdev.ooms.hr.contract.entity.Contract;
import com.xdev.ooms.hr.contract.service.ContractService;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiResponse;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import com.xdev.ooms.sharedkernel.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hr/contract")
public class ContractController extends BaseControllerImpl<Contract, ContractDto, ContractDto> {

    private final ContractService contractService;

    public ContractController(ContractService contractService, BaseService<Contract, ContractDto, ContractDto> baseService, ModelMapper modelMapper) {
        super(baseService, modelMapper);
        this.contractService = contractService;
    }

    @Override
    protected String getResourceName() {
        return "CONTRACT";
    }

    //  Ajouter un contrat à un employé
    @PostMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<Contract, ContractDto>> addContractToEmployee(
            @PathVariable UUID employeeId,
            @RequestBody ContractDto contractDto) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "addContractToEmployee", employeeId);
        try {
            return ResponseEntity.ok(new ApiResponse<>(true, "", List.of(contractService.addContractToEmployee(employeeId, contractDto))));


        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "addContractToEmployee", e);
            throw e;
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "addContractToEmployee", null);
            OSMLogger.logPerformance(this.getClass(), "addContractToEmployee", startTime, System.currentTimeMillis());
        }  }

    //  Récupérer tous les contrats d'un employé
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<Contract, ContractDto>> getContractsByEmployee(@PathVariable UUID employeeId) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getContractsByEmployee", employeeId);
        try {
            List<ContractDto> contracts = contractService.getContractsByEmployee(employeeId);
            return ResponseEntity.ok(new ApiResponse<>(true, "", contracts));


         } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "getContractsByEmployee", e);
            throw e;
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "getContractsByEmployee", null);
            OSMLogger.logPerformance(this.getClass(), "getContractsByEmployee", startTime, System.currentTimeMillis());
        }


    }

    // Mettre à jour un contrat lié à un employé
    @PutMapping("/employee/{employeeId}/{contractId}")
    public ResponseEntity<ApiResponse<Contract, ContractDto>> updateEmployeeContract(
            @PathVariable UUID employeeId,
            @PathVariable UUID contractId,
            @RequestBody ContractDto contractDto) {
        ContractDto updatedContract = contractService.updateEmployeeContract(employeeId, contractId, contractDto);
        return ResponseEntity.ok(new ApiResponse<>(true, "", List.of(updatedContract)));
    }
    // Supprimer un contrat d'un employé

    @DeleteMapping("/employee/{employeeId}/{contractId}")
    public ResponseEntity<ApiResponse<Contract, ContractDto>> deleteEmployeeContract(
            @PathVariable UUID employeeId,
            @PathVariable UUID contractId) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "deleteEmployeeContract", employeeId, contractId);
        try {
            contractService.deleteEmployeeContract(employeeId, contractId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Contract deleted successfully", List.of()));
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "deleteEmployeeContract", e);
            throw e;
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "deleteEmployeeContract", null);
            OSMLogger.logPerformance(this.getClass(), "deleteEmployeeContract", startTime, System.currentTimeMillis());
        }
    }


}
