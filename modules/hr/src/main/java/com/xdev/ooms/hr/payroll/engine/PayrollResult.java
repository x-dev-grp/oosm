package com.xdev.ooms.hr.payroll.engine;

import com.xdev.ooms.hr.legal.dto.PayrollLegalSnapshot;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PayrollResult {

    private BigDecimal baseSalary;
    private BigDecimal grossSalary;
    private BigDecimal taxableSalary;
    private BigDecimal cnssBase;
    private BigDecimal employeeCnss;
    private BigDecimal employerCnss;
    private BigDecimal css;
    private BigDecimal incomeTax;
    private BigDecimal totalDeductions;
    private BigDecimal netSalary;
    private BigDecimal employerCost;
    private BigDecimal accidentContribution;
    private BigDecimal otherDeductions;
    private List<PayrollLineResult> lines = new ArrayList<>();
    private LinkedHashMap<String, BigDecimal> breakdown = new LinkedHashMap<>();
    private String legalSnapshotJson;
    private PayrollLegalSnapshot legalSnapshot;
    private List<String> anomalies = new ArrayList<>();

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public BigDecimal getGrossSalary() {
        return grossSalary;
    }

    public void setGrossSalary(BigDecimal grossSalary) {
        this.grossSalary = grossSalary;
    }

    public BigDecimal getTaxableSalary() {
        return taxableSalary;
    }

    public void setTaxableSalary(BigDecimal taxableSalary) {
        this.taxableSalary = taxableSalary;
    }

    public BigDecimal getCnssBase() {
        return cnssBase;
    }

    public void setCnssBase(BigDecimal cnssBase) {
        this.cnssBase = cnssBase;
    }

    public BigDecimal getEmployeeCnss() {
        return employeeCnss;
    }

    public void setEmployeeCnss(BigDecimal employeeCnss) {
        this.employeeCnss = employeeCnss;
    }

    public BigDecimal getEmployerCnss() {
        return employerCnss;
    }

    public void setEmployerCnss(BigDecimal employerCnss) {
        this.employerCnss = employerCnss;
    }

    public BigDecimal getCss() {
        return css;
    }

    public void setCss(BigDecimal css) {
        this.css = css;
    }

    public BigDecimal getIncomeTax() {
        return incomeTax;
    }

    public void setIncomeTax(BigDecimal incomeTax) {
        this.incomeTax = incomeTax;
    }

    public BigDecimal getTotalDeductions() {
        return totalDeductions;
    }

    public void setTotalDeductions(BigDecimal totalDeductions) {
        this.totalDeductions = totalDeductions;
    }

    public BigDecimal getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(BigDecimal netSalary) {
        this.netSalary = netSalary;
    }

    public BigDecimal getEmployerCost() {
        return employerCost;
    }

    public void setEmployerCost(BigDecimal employerCost) {
        this.employerCost = employerCost;
    }

    public BigDecimal getAccidentContribution() {
        return accidentContribution;
    }

    public void setAccidentContribution(BigDecimal accidentContribution) {
        this.accidentContribution = accidentContribution;
    }

    public BigDecimal getOtherDeductions() {
        return otherDeductions;
    }

    public void setOtherDeductions(BigDecimal otherDeductions) {
        this.otherDeductions = otherDeductions;
    }

    public List<PayrollLineResult> getLines() {
        return lines;
    }

    public void setLines(List<PayrollLineResult> lines) {
        this.lines = lines != null ? lines : new ArrayList<>();
    }

    public LinkedHashMap<String, BigDecimal> getBreakdown() {
        return breakdown;
    }

    public void setBreakdown(LinkedHashMap<String, BigDecimal> breakdown) {
        this.breakdown = breakdown != null ? breakdown : new LinkedHashMap<>();
    }

    public void putBreakdown(String step, BigDecimal amount) {
        this.breakdown.put(step, amount);
    }

    public String getLegalSnapshotJson() {
        return legalSnapshotJson;
    }

    public void setLegalSnapshotJson(String legalSnapshotJson) {
        this.legalSnapshotJson = legalSnapshotJson;
    }

    public PayrollLegalSnapshot getLegalSnapshot() {
        return legalSnapshot;
    }

    public void setLegalSnapshot(PayrollLegalSnapshot legalSnapshot) {
        this.legalSnapshot = legalSnapshot;
    }

    public List<String> getAnomalies() {
        return anomalies;
    }

    public void setAnomalies(List<String> anomalies) {
        this.anomalies = anomalies != null ? anomalies : new ArrayList<>();
    }

    public void addAnomaly(String anomaly) {
        this.anomalies.add(anomaly);
    }

    public Map<String, BigDecimal> breakdownAsMap() {
        return breakdown;
    }
}
