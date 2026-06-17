package com.xdev.ooms.finance.billing.service;

public record BillDocument(String invoiceNumber, String fileName, String mediaType, byte[] content) {
}
