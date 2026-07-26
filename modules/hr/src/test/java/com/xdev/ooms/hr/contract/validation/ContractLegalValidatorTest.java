package com.xdev.ooms.hr.contract.validation;

import com.xdev.ooms.hr.common.enums.CddLegalReason;
import com.xdev.ooms.hr.common.enums.ContractType;
import com.xdev.ooms.hr.contract.entity.EmploymentContract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContractLegalValidatorTest {

    private ContractLegalValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ContractLegalValidator();
    }

    @Test
    void cdiValidContract_doesNotFlagCddWithoutLegalReason() {
        EmploymentContract contract = baseContract(ContractType.CDI);
        contract.setEndDate(null);
        contract.setCddLegalReason(null);

        List<String> violations = validator.validate(contract);

        assertFalse(violations.contains("CDD_WITHOUT_LEGAL_REASON"));
        assertFalse(violations.contains("CDD_MISSING_END_DATE"));
        assertTrue(violations.isEmpty());
    }

    @Test
    void cddWithoutLegalReason_flagsCddWithoutLegalReason() {
        EmploymentContract contract = baseContract(ContractType.CDD);
        contract.setEndDate(LocalDate.of(2026, 12, 31));
        contract.setCddLegalReason(null);

        List<String> violations = validator.validate(contract);

        assertTrue(violations.contains("CDD_WITHOUT_LEGAL_REASON"));
    }

    @Test
    void cddWithLegalReason_okForLegalReasonRule() {
        EmploymentContract contract = baseContract(ContractType.CDD);
        contract.setEndDate(LocalDate.of(2026, 12, 31));
        contract.setCddLegalReason(CddLegalReason.TEMPORARY_INCREASE_ACTIVITY);

        List<String> violations = validator.validate(contract);

        assertFalse(violations.contains("CDD_WITHOUT_LEGAL_REASON"));
    }

    @Test
    void endBeforeStart_flagsInvalidContractDates() {
        EmploymentContract contract = baseContract(ContractType.CDI);
        contract.setStartDate(LocalDate.of(2026, 6, 1));
        contract.setEndDate(LocalDate.of(2026, 1, 1));

        List<String> violations = validator.validate(contract);

        assertTrue(violations.contains("INVALID_CONTRACT_DATES"));
    }

    @Test
    void missingSalaryAndBaseSalary_flagsContractWithoutSalary() {
        EmploymentContract contract = baseContract(ContractType.CDI);
        contract.setBaseSalary(null);
        contract.setSalary(null);

        List<String> violations = validator.validate(contract);

        assertTrue(violations.contains("CONTRACT_WITHOUT_SALARY"));
    }

    @Test
    void zeroSalary_flagsContractWithoutSalary() {
        EmploymentContract contract = baseContract(ContractType.CDI);
        contract.setBaseSalary(BigDecimal.ZERO);
        contract.setSalary(0.0);

        List<String> violations = validator.validate(contract);

        assertTrue(violations.contains("CONTRACT_WITHOUT_SALARY"));
    }

    @Test
    void cddMissingEndDate_flagsCddMissingEndDate() {
        EmploymentContract contract = baseContract(ContractType.CDD);
        contract.setEndDate(null);
        contract.setCddLegalReason(CddLegalReason.SEASONAL_WORK);

        List<String> violations = validator.validate(contract);

        assertTrue(violations.contains("CDD_MISSING_END_DATE"));
    }

    private static EmploymentContract baseContract(ContractType type) {
        EmploymentContract contract = new EmploymentContract();
        contract.setContractType(type);
        contract.setStartDate(LocalDate.of(2026, 1, 1));
        contract.setBaseSalary(new BigDecimal("5000.00"));
        return contract;
    }
}
