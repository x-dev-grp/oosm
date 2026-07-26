package com.xdev.ooms.hr.contract.validation;

import com.xdev.ooms.hr.common.enums.ContractType;
import com.xdev.ooms.hr.contract.entity.EmploymentContract;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Component
public class ContractLegalValidator {

    private static final EnumSet<ContractType> REQUIRES_LEGAL_REASON = EnumSet.of(
            ContractType.CDD,
            ContractType.SEASONAL,
            ContractType.TEMPORARY
    );

    private static final EnumSet<ContractType> FIXED_TERM_TYPES = EnumSet.of(
            ContractType.CDD,
            ContractType.TEMPORARY,
            ContractType.INTERNSHIP,
            ContractType.SEASONAL,
            ContractType.OTHER_LEGAL_TYPE
    );

    public List<String> validate(EmploymentContract contract) {
        List<String> violations = new ArrayList<>();
        if (contract == null) {
            return violations;
        }

        ContractType type = contract.getContractType();
        if (type != null && REQUIRES_LEGAL_REASON.contains(type) && contract.getCddLegalReason() == null) {
            violations.add("CDD_WITHOUT_LEGAL_REASON");
        }

        LocalDate start = contract.getStartDate();
        LocalDate end = contract.getEndDate();
        if (start != null && end != null && end.isBefore(start)) {
            violations.add("INVALID_CONTRACT_DATES");
        }

        if (isInvalidProbation(contract.getProbationStart(), contract.getProbationEnd(), start, end)) {
            violations.add("INVALID_PROBATION_PERIOD");
        }

        if (!hasPositiveSalary(contract)) {
            violations.add("CONTRACT_WITHOUT_SALARY");
        }

        if (type != null && FIXED_TERM_TYPES.contains(type) && end == null) {
            violations.add("CDD_MISSING_END_DATE");
        }

        return violations;
    }

    private boolean isInvalidProbation(
            LocalDate probationStart,
            LocalDate probationEnd,
            LocalDate contractStart,
            LocalDate contractEnd
    ) {
        if (probationStart == null && probationEnd == null) {
            return false;
        }
        if (probationStart != null && probationEnd != null && probationEnd.isBefore(probationStart)) {
            return true;
        }
        if (contractStart != null) {
            if (probationStart != null && probationStart.isBefore(contractStart)) {
                return true;
            }
            if (probationEnd != null && probationEnd.isBefore(contractStart)) {
                return true;
            }
        }
        if (contractEnd != null) {
            if (probationStart != null && probationStart.isAfter(contractEnd)) {
                return true;
            }
            if (probationEnd != null && probationEnd.isAfter(contractEnd)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPositiveSalary(EmploymentContract contract) {
        if (contract.getBaseSalary() != null && contract.getBaseSalary().compareTo(BigDecimal.ZERO) > 0) {
            return true;
        }
        return contract.getSalary() != null && contract.getSalary() > 0;
    }
}
