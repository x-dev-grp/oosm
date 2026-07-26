package com.xdev.ooms.hr.legal.entity;

import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hr_tax_configuration")
public class TaxConfiguration extends BaseEntity implements Serializable {

    private Integer fiscalYear;

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer version = 1;

    private Boolean active = true;

    @OneToMany(mappedBy = "taxConfiguration", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<TaxBracket> brackets = new ArrayList<>();

    public Integer getFiscalYear() {
        return fiscalYear;
    }

    public void setFiscalYear(Integer fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<TaxBracket> getBrackets() {
        return brackets;
    }

    public void setBrackets(List<TaxBracket> brackets) {
        this.brackets = brackets;
    }
}
