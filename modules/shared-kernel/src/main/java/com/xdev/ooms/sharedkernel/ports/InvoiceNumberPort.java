package com.xdev.ooms.sharedkernel.ports;

/**
 * Generates sequential invoice / bill numbers (format INV-YY-NNN).
 * The sequence is shared across financial documents in the application.
 */
public interface InvoiceNumberPort {

    String nextInvoiceNumber();
}
