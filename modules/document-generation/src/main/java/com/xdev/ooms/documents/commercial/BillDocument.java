package com.xdev.ooms.documents.commercial;

public record BillDocument(String invoiceNumber, String fileName, String mediaType, byte[] content) {
}
