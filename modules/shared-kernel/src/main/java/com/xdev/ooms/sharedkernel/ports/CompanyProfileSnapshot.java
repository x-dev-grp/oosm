package com.xdev.ooms.sharedkernel.ports;

/**
 * Billing / PDF snapshot of the current tenant company profile (no JPA entity leakage).
 */
public record CompanyProfileSnapshot(
        String legalName,
        String address,
        String taxId,
        String phone,
        String website,
        String logoBase64,
        String logoContentType,
        String cnssNumber,
        String registrationNumber,
        String invoiceFooterNote,
        String invoiceLegalMentions,
        String invoiceBankName,
        String invoiceBankIban,
        String invoiceBankSwift) {
}
