package com.xdev.ooms.documents.commercial;

/**
 * How bill line unit prices relate to VAT on the generated PDF.
 */
public enum BillVatMode {
    /** Unit prices are HT; VAT is added on top (legacy behaviour). */
    STANDARD,
    /** Unit prices are TTC; VAT is reverse-calculated from the inclusive amount. */
    INCLUSIVE,
    /** No VAT columns or amounts (sales bills). */
    NONE
}
