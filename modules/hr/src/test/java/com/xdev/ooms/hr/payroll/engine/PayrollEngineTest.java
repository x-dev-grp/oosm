package com.xdev.ooms.hr.payroll.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xdev.ooms.hr.common.HrMoney;
import com.xdev.ooms.hr.legal.dto.PayrollLegalSnapshot;
import com.xdev.ooms.hr.legal.entity.MinimumWageRule;
import com.xdev.ooms.hr.legal.entity.SocialSecurityConfiguration;
import com.xdev.ooms.hr.legal.entity.TaxBracket;
import com.xdev.ooms.hr.legal.entity.TaxConfiguration;
import com.xdev.ooms.hr.legal.enums.WeeklyRegimeType;
import com.xdev.ooms.hr.legal.repository.CompanyHrLegalProfileRepository;
import com.xdev.ooms.hr.legal.service.LegalConfigurationSeedService;
import com.xdev.ooms.hr.legal.service.LegalConfigurationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PayrollEngineTest {

    private PayrollEngine payrollEngine;
    private SocialSecurityConfiguration social;
    private TaxConfiguration taxConfiguration;
    private MinimumWageRule minimumWage;

    @BeforeEach
    void setUp() {
        LegalConfigurationService legalConfigurationService = mock(LegalConfigurationService.class);
        CompanyHrLegalProfileRepository companyRepo = mock(CompanyHrLegalProfileRepository.class);

        GrossSalaryCalculator grossSalaryCalculator = new GrossSalaryCalculator();
        AbsenceDeductionCalculator absenceDeductionCalculator = new AbsenceDeductionCalculator();
        SocialSecurityCalculator socialSecurityCalculator = new SocialSecurityCalculator();
        IncomeTaxCalculator incomeTaxCalculator = new IncomeTaxCalculator();
        NetSalaryCalculator netSalaryCalculator = new NetSalaryCalculator();
        EmployerCostCalculator employerCostCalculator = new EmployerCostCalculator();
        PayrollValidator payrollValidator = new PayrollValidator(legalConfigurationService);
        PayrollContextBuilder payrollContextBuilder = new PayrollContextBuilder();
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        payrollEngine = new PayrollEngine(
                legalConfigurationService,
                companyRepo,
                grossSalaryCalculator,
                absenceDeductionCalculator,
                socialSecurityCalculator,
                incomeTaxCalculator,
                netSalaryCalculator,
                employerCostCalculator,
                payrollValidator,
                payrollContextBuilder,
                objectMapper
        );

        social = new SocialSecurityConfiguration();
        social.setEmployeeRate(HrMoney.rate(LegalConfigurationSeedService.PROVISIONAL_CNSS_EMPLOYEE));
        social.setEmployerRate(HrMoney.rate(LegalConfigurationSeedService.PROVISIONAL_CNSS_EMPLOYER));
        social.setCssRate(HrMoney.rate(LegalConfigurationSeedService.PROVISIONAL_CSS));
        social.setAccidentContributionRate(HrMoney.rate(BigDecimal.ZERO));

        taxConfiguration = new TaxConfiguration();
        List<TaxBracket> brackets = new ArrayList<>();
        brackets.add(bracket("0", "1500", "0", 1));
        brackets.add(bracket("1500", "5000", "0.15", 2));
        brackets.add(bracket("5000", "10000", "0.25", 3));
        brackets.add(bracket("10000", "20000", "0.30", 4));
        brackets.add(bracket("20000", null, "0.35", 5));
        taxConfiguration.setBrackets(brackets);

        minimumWage = new MinimumWageRule();
        minimumWage.setWeeklyRegime(WeeklyRegimeType.HOURS_48);
        minimumWage.setMonthlyMinimum(HrMoney.money(LegalConfigurationSeedService.PROVISIONAL_SMIG_48H));
    }

    @Test
    void calculatesFullPayslipForBaseSalary() {
        PayrollInputs inputs = new PayrollInputs();
        PayrollLegalSnapshot snapshot = new PayrollLegalSnapshot();
        snapshot.setAsOf(LocalDate.of(2025, 6, 30));

        PayrollResult result = payrollEngine.calculateWithConfig(
                new BigDecimal("2000"),
                inputs,
                social,
                taxConfiguration,
                minimumWage,
                snapshot
        );

        assertEquals(HrMoney.money(new BigDecimal("2000")), result.getBaseSalary());
        assertEquals(HrMoney.money(new BigDecimal("2000")), result.getGrossSalary());
        assertEquals(HrMoney.money(new BigDecimal("193.600")), result.getEmployeeCnss());
        assertEquals(HrMoney.money(new BigDecimal("341.400")), result.getEmployerCnss());
        assertEquals(HrMoney.money(new BigDecimal("10.000")), result.getCss());
        assertEquals(HrMoney.money(new BigDecimal("1806.400")), result.getTaxableSalary());
        assertEquals(HrMoney.money(new BigDecimal("45.960")), result.getIncomeTax());
        assertEquals(HrMoney.money(new BigDecimal("1750.440")), result.getNetSalary());
        assertEquals(HrMoney.money(new BigDecimal("2341.400")), result.getEmployerCost());
        assertTrue(result.getAnomalies().isEmpty());
        assertTrue(result.getBreakdown().containsKey("netSalary"));
        assertTrue(result.getLines().stream().anyMatch(l -> "CNSS_EE".equals(l.getComponentCode())));
    }

    @Test
    void addsBonusesAndFlagsSalaryBelowMinimum() {
        PayrollInputs inputs = new PayrollInputs();
        inputs.setBonuses(new BigDecimal("50"));

        PayrollResult result = payrollEngine.calculateWithConfig(
                new BigDecimal("400"),
                inputs,
                social,
                taxConfiguration,
                minimumWage,
                new PayrollLegalSnapshot()
        );

        assertEquals(HrMoney.money(new BigDecimal("450")), result.getGrossSalary());
        assertTrue(result.getAnomalies().contains(PayrollValidator.SALARY_BELOW_MINIMUM));
    }

    @Test
    void appliesAbsenceAndOtherDeductions() {
        PayrollInputs inputs = new PayrollInputs();
        inputs.setAbsenceDeduction(new BigDecimal("100"));
        inputs.setAdvanceDeduction(new BigDecimal("50"));

        PayrollResult result = payrollEngine.calculateWithConfig(
                new BigDecimal("2000"),
                inputs,
                social,
                taxConfiguration,
                minimumWage,
                new PayrollLegalSnapshot()
        );

        assertEquals(HrMoney.money(new BigDecimal("1900")), result.getGrossSalary());
        assertTrue(result.getNetSalary().compareTo(HrMoney.money(new BigDecimal("1600"))) > 0);
        assertEquals(HrMoney.money(new BigDecimal("50")), result.getOtherDeductions());
    }

    private static TaxBracket bracket(String min, String max, String rate, int sortOrder) {
        TaxBracket b = new TaxBracket();
        b.setMinAmount(HrMoney.money(new BigDecimal(min)));
        b.setMaxAmount(max != null ? HrMoney.money(new BigDecimal(max)) : null);
        b.setRate(HrMoney.rate(new BigDecimal(rate)));
        b.setSortOrder(sortOrder);
        return b;
    }
}
