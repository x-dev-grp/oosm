package com.xdev.ooms.conditioning.certification.entity;

import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
@Entity
public class Certification extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String code;

    @Column(length = 1000)
    private String description;

    private String issuingBody;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String logoData;

    private String logoContentType;

    private String websiteUrl;

    private String category; // e.g., LEGAL, MARKETING, QUALITY

    private Boolean isActive = true;

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getIssuingBody() {
        return issuingBody;
    }

    public String getLogoData() {
        return logoData;
    }

    public String getLogoContentType() {
        return logoContentType;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public String getCategory() {
        return category;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIssuingBody(String issuingBody) {
        this.issuingBody = issuingBody;
    }

    public void setLogoData(String logoData) {
        this.logoData = logoData;
    }

    public void setLogoContentType(String logoContentType) {
        this.logoContentType = logoContentType;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
