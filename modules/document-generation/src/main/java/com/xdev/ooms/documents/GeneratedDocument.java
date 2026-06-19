package com.xdev.ooms.documents;

public record GeneratedDocument(String reference, String fileName, String mediaType, byte[] content) {

    public static GeneratedDocument pdf(String reference, String fileName, byte[] content) {
        return new GeneratedDocument(reference, fileName, "application/pdf", content);
    }

    public static GeneratedDocument fromForm(String fileName, byte[] content) {
        return pdf(null, fileName, content);
    }

    public static GeneratedDocument fromBill(com.xdev.ooms.documents.commercial.BillDocument bill) {
        return pdf(bill.invoiceNumber(), bill.fileName(), bill.content());
    }
}
