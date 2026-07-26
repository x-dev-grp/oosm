package com.xdev.ooms.hr.payroll.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xdev.ooms.hr.common.HrMoney;
import com.xdev.ooms.hr.common.enums.WorkRegime;
import com.xdev.ooms.hr.legal.dto.PayrollLegalSnapshot;
import com.xdev.ooms.hr.legal.entity.CompanyHrLegalProfile;
import com.xdev.ooms.hr.legal.entity.MinimumWageRule;
import com.xdev.ooms.hr.legal.entity.SocialSecurityConfiguration;
import com.xdev.ooms.hr.legal.entity.TaxConfiguration;
import com.xdev.ooms.hr.legal.enums.WeeklyRegimeType;
import com.xdev.ooms.hr.legal.repository.CompanyHrLegalProfileRepository;
import com.xdev.ooms.hr.legal.service.LegalConfigurationService;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

/**
 * Deterministic Tunisian payroll calculation orchestrator.
 * Rates and brackets are always loaded from {@link LegalConfigurationService}.
 */
@Component
public class PayrollEngine {

    private final LegalConfigurationService legalConfigurationService;
    private final CompanyHrLegalProfileRepository companyHrLegalProfileRepository;
    private final GrossSalaryCalculator grossSalaryCalculator;
    private final AbsenceDeductionCalculator absenceDeductionCalculator;
    private final SocialSecurityCalculator socialSecurityCalculator;
    private final IncomeTaxCalculator incomeTaxCalculator;
    private final NetSalaryCalculator netSalaryCalculator;
    private final EmployerCostCalculator employerCostCalculator;
    private final PayrollValidator payrollValidator;
    private final PayrollContextBuilder payrollContextBuilder;
    private final ObjectMapper objectMapper;

    public PayrollEngine(
            LegalConfigurationService legalConfigurationService,
            CompanyHrLegalProfileRepository companyHrLegalProfileRepository,
            GrossSalaryCalculator grossSalaryCalculator,
            AbsenceDeductionCalculator absenceDeductionCalculator,
            SocialSecurityCalculator socialSecurityCalculator,
            IncomeTaxCalculator incomeTaxCalculator,
            NetSalaryCalculator netSalaryCalculator,
            EmployerCostCalculator employerCostCalculator,
            PayrollValidator payrollValidator,
            PayrollContextBuilder payrollContextBuilder,
            ObjectMapper objectMapper
    ) {
        this.legalConfigurationService = legalConfigurationService;
        this.companyHrLegalProfileRepository = companyHrLegalProfileRepository;
        this.grossSalaryCalculator = grossSalaryCalculator;
        this.absenceDeductionCalculator = absenceDeductionCalculator;
        this.socialSecurityCalculator = socialSecurityCalculator;
        this.incomeTaxCalculator = incomeTaxCalculator;
        this.netSalaryCalculator = netSalaryCalculator;
        this.employerCostCalculator = employerCostCalculator;
        this.payrollValidator = payrollValidator;
        this.payrollContextBuilder = payrollContextBuilder;
        this.objectMapper = objectMapper;
    }

    public PayrollResult calculate(
            BigDecimal baseSalary,
            PayrollInputs inputs,
            LocalDate periodEnd,
            WeeklyRegimeType workRegime
    ) {
        LocalDate asOf = periodEnd != null ? periodEnd : LocalDate.now();
        WeeklyRegimeType regime = workRegime != null ? workRegime : WeeklyRegimeType.HOURS_48;
        PayrollInputs safeInputs = inputs != null ? inputs : new PayrollInputs();

        SocialSecurityConfiguration social = legalConfigurationService.resolveSocialSecurity(asOf);
        TaxConfiguration taxConfig = legalConfigurationService.resolveTaxConfiguration(asOf);
        if (social == null || taxConfig == null) {
            throw new IllegalStateException(
                    "Legal payroll configuration is missing. Seed legal rules via POST /api/hr/legal-rules/seed-defaults");
        }

        String wageProfile = resolveMinimumWageProfile();
        MinimumWageRule minimumWage = legalConfigurationService.resolveMinimumWage(wageProfile, regime, asOf);
        PayrollLegalSnapshot snapshot = legalConfigurationService.buildPayrollLegalSnapshot(asOf);

        return calculateWithConfig(
                HrMoney.money(baseSalary),
                safeInputs,
                social,
                taxConfig,
                minimumWage,
                snapshot
        );
    }

    public PayrollResult calculate(
            BigDecimal baseSalary,
            PayrollInputs inputs,
            LocalDate periodEnd,
            WorkRegime workRegime
    ) {
        return calculate(
                baseSalary,
                inputs,
                periodEnd,
                payrollContextBuilder.mapWorkRegime(workRegime)
        );
    }

    /**
     * Pure calculation path for unit tests — configuration objects are passed in.
     */
    public PayrollResult calculateWithConfig(
            BigDecimal baseSalary,
            PayrollInputs inputs,
            SocialSecurityConfiguration social,
            TaxConfiguration taxConfig,
            MinimumWageRule minimumWage,
            PayrollLegalSnapshot snapshot
    ) {
        PayrollInputs safeInputs = inputs != null ? inputs : new PayrollInputs();
        BigDecimal base = HrMoney.money(baseSalary);

        BigDecimal grossBeforeAbsence = grossSalaryCalculator.calculate(base, safeInputs);
        BigDecimal absence = safeInputs.getAbsenceDeduction();
        BigDecimal gross = absenceDeductionCalculator.apply(grossBeforeAbsence, absence);
        BigDecimal cnssBase = gross;

        SocialSecurityAmounts socialAmounts = socialSecurityCalculator.calculate(cnssBase, social);
        BigDecimal taxable = incomeTaxCalculator.resolveTaxableSalary(gross, socialAmounts.getEmployeeCnss());
        BigDecimal incomeTax = incomeTaxCalculator.calculate(taxable, taxConfig);

        BigDecimal advances = safeInputs.getAdvanceDeduction();
        BigDecimal loans = safeInputs.getLoanDeduction();
        BigDecimal otherDeductions = safeInputs.getOtherDeductions();

        BigDecimal totalDeductions = netSalaryCalculator.totalEmployeeDeductions(
                socialAmounts.getEmployeeCnss(),
                incomeTax,
                socialAmounts.getCss(),
                advances,
                loans,
                otherDeductions
        );
        BigDecimal net = netSalaryCalculator.calculate(
                gross,
                socialAmounts.getEmployeeCnss(),
                incomeTax,
                socialAmounts.getCss(),
                advances,
                loans,
                otherDeductions
        );
        BigDecimal employerCost = employerCostCalculator.calculate(
                gross,
                socialAmounts.getEmployerCnss(),
                socialAmounts.getAccidentContribution()
        );

        PayrollResult result = new PayrollResult();
        result.setBaseSalary(base);
        result.setGrossSalary(gross);
        result.setTaxableSalary(taxable);
        result.setCnssBase(cnssBase);
        result.setEmployeeCnss(socialAmounts.getEmployeeCnss());
        result.setEmployerCnss(socialAmounts.getEmployerCnss());
        result.setCss(socialAmounts.getCss());
        result.setIncomeTax(incomeTax);
        result.setTotalDeductions(totalDeductions);
        result.setNetSalary(net);
        result.setEmployerCost(employerCost);
        result.setAccidentContribution(socialAmounts.getAccidentContribution());
        result.setOtherDeductions(HrMoney.money(
                HrMoney.money(safe(advances))
                        .add(HrMoney.money(safe(loans)), HrMoney.MC)
                        .add(HrMoney.money(safe(otherDeductions)), HrMoney.MC)));
        result.setLines(buildLines(base, safeInputs, grossBeforeAbsence, gross, socialAmounts, incomeTax, net, employerCost));
        result.setBreakdown(buildBreakdown(
                base, grossBeforeAbsence, gross, cnssBase, socialAmounts, taxable, incomeTax, totalDeductions, net, employerCost));
        result.setLegalSnapshot(snapshot);
        result.setLegalSnapshotJson(toJson(snapshot));
        result.setAnomalies(new ArrayList<>(payrollValidator.validateAgainstMinimum(base, minimumWage)));
        return result;
    }

    private List<PayrollLineResult> buildLines(
            BigDecimal base,
            PayrollInputs inputs,
            BigDecimal grossBeforeAbsence,
            BigDecimal gross,
            SocialSecurityAmounts social,
            BigDecimal incomeTax,
            BigDecimal net,
            BigDecimal employerCost
    ) {
        List<PayrollLineResult> lines = new ArrayList<>();
        lines.add(line("BASE", "Salaire de base", PayrollLineType.EARNING, base, true, true));
        addIfPositive(lines, "OVERTIME", "Heures supplémentaires", PayrollLineType.EARNING,
                inputs.getOvertimeAmount(), true, true);
        addIfPositive(lines, "BONUS", "Primes", PayrollLineType.EARNING, inputs.getBonuses(), true, true);
        addIfPositive(lines, "OTHER_EARNING", "Autres gains", PayrollLineType.EARNING,
                inputs.getOtherEarnings(), true, true);
        addIfPositive(lines, "ABSENCE", "Retenue absences", PayrollLineType.DEDUCTION,
                inputs.getAbsenceDeduction(), false, false);
        lines.add(line("GROSS", "Salaire brut", PayrollLineType.INFORMATIONAL, gross, false, false));
        lines.add(line("CNSS_EE", "CNSS salarié", PayrollLineType.DEDUCTION, social.getEmployeeCnss(), false, false));
        lines.add(line("CSS", "CSS", PayrollLineType.DEDUCTION, social.getCss(), false, false));
        lines.add(line("IRPP", "IRPP", PayrollLineType.DEDUCTION, incomeTax, false, false));
        addIfPositive(lines, "ADVANCE", "Avance", PayrollLineType.DEDUCTION, inputs.getAdvanceDeduction(), false, false);
        addIfPositive(lines, "LOAN", "Prêt", PayrollLineType.DEDUCTION, inputs.getLoanDeduction(), false, false);
        addIfPositive(lines, "OTHER_DED", "Autres retenues", PayrollLineType.DEDUCTION,
                inputs.getOtherDeductions(), false, false);
        lines.add(line("NET", "Net à payer", PayrollLineType.INFORMATIONAL, net, false, false));
        lines.add(line("CNSS_ER", "CNSS employeur", PayrollLineType.EMPLOYER_CONTRIBUTION,
                social.getEmployerCnss(), false, false));
        if (social.getAccidentContribution() != null
                && social.getAccidentContribution().compareTo(BigDecimal.ZERO) > 0) {
            lines.add(line("ACCIDENT", "Accident du travail", PayrollLineType.EMPLOYER_CONTRIBUTION,
                    social.getAccidentContribution(), false, false));
        }
        lines.add(line("EMPLOYER_COST", "Coût employeur", PayrollLineType.INFORMATIONAL, employerCost, false, false));
        lines.add(line("GROSS_BEFORE_ABSENCE", "Brut avant absences", PayrollLineType.INFORMATIONAL,
                grossBeforeAbsence, false, false));
        return lines;
    }

    private LinkedHashMap<String, BigDecimal> buildBreakdown(
            BigDecimal base,
            BigDecimal grossBeforeAbsence,
            BigDecimal gross,
            BigDecimal cnssBase,
            SocialSecurityAmounts social,
            BigDecimal taxable,
            BigDecimal incomeTax,
            BigDecimal totalDeductions,
            BigDecimal net,
            BigDecimal employerCost
    ) {
        LinkedHashMap<String, BigDecimal> breakdown = new LinkedHashMap<>();
        breakdown.put("baseSalary", base);
        breakdown.put("grossBeforeAbsence", grossBeforeAbsence);
        breakdown.put("grossSalary", gross);
        breakdown.put("cnssBase", cnssBase);
        breakdown.put("employeeCnss", social.getEmployeeCnss());
        breakdown.put("employerCnss", social.getEmployerCnss());
        breakdown.put("css", social.getCss());
        breakdown.put("accidentContribution", social.getAccidentContribution());
        breakdown.put("taxableSalary", taxable);
        breakdown.put("incomeTax", incomeTax);
        breakdown.put("totalDeductions", totalDeductions);
        breakdown.put("netSalary", net);
        breakdown.put("employerCost", employerCost);
        return breakdown;
    }

    private String resolveMinimumWageProfile() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            return "SMIG";
        }
        return companyHrLegalProfileRepository
                .findFirstByTenantIdAndIsDeletedFalseOrderByCreatedDateDesc(tenantId)
                .map(CompanyHrLegalProfile::getMinimumWageProfile)
                .filter(p -> p != null && !p.isBlank())
                .orElse("SMIG");
    }

    private String toJson(PayrollLegalSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private static void addIfPositive(
            List<PayrollLineResult> lines,
            String code,
            String label,
            PayrollLineType type,
            BigDecimal amount,
            boolean taxable,
            boolean cnssApplicable
    ) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            lines.add(line(code, label, type, HrMoney.money(amount), taxable, cnssApplicable));
        }
    }

    private static PayrollLineResult line(
            String code,
            String label,
            PayrollLineType type,
            BigDecimal amount,
            boolean taxable,
            boolean cnssApplicable
    ) {
        return new PayrollLineResult(code, label, type, HrMoney.money(amount), taxable, cnssApplicable);
    }

    private static BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
