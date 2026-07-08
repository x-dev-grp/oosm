package com.xdev.ooms.documents.commercial;

import com.xdev.ooms.documents.commercial.dto.BillLineDto;
import com.xdev.ooms.documents.commercial.dto.BillTotalsDto;
import com.xdev.ooms.sharedkernel.Enum.OperationType;
import com.xdev.ooms.sharedkernel.Enum.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BillVatCalculatorTest {

    @Test
    void inclusiveModeExtractsVatFromTtcPrice() {
        BillLineDto line = new BillLineDto();
        line.setQuantity(BigDecimal.ONE);
        line.setUnitPriceExcludingVat(new BigDecimal("119"));
        line.setVatRatePercent(TunisiaVatDefaults.STANDARD_RATE);

        BillTotalsDto totals = BillVatCalculator.calculateTotals(BillVatMode.INCLUSIVE, List.of(line));

        assertEquals(new BigDecimal("100.000"), totals.getSubtotalExcludingVat());
        assertEquals(new BigDecimal("19.000"), totals.getVatAmount());
        assertEquals(new BigDecimal("119.000"), totals.getTotalIncludingVat());
    }

    @Test
    void noneModeKeepsSalesTotalWithoutVat() {
        BillLineDto line = new BillLineDto();
        line.setQuantity(new BigDecimal("2"));
        line.setUnitPriceExcludingVat(new BigDecimal("50"));
        line.setVatRatePercent(BigDecimal.ZERO);

        BillTotalsDto totals = BillVatCalculator.calculateTotals(BillVatMode.NONE, List.of(line));

        assertEquals(new BigDecimal("100.000"), totals.getSubtotalExcludingVat());
        assertEquals(BigDecimal.ZERO.setScale(3), totals.getVatAmount());
        assertEquals(new BigDecimal("100.000"), totals.getTotalIncludingVat());
    }

    @Test
    void flowClassifierDistinguishesPurchaseAndSale() {
        assertTrue(BillFlowClassifier.isPurchase(OperationType.OIL_PURCHASE, null));
        assertTrue(BillFlowClassifier.isSale(OperationType.OIL_SALE, null));
        assertFalse(BillFlowClassifier.isSale(OperationType.OIL_PURCHASE, null));
        assertTrue(BillFlowClassifier.isPurchase(null, TransactionType.SUPPLIER_PAYMENT));
        assertTrue(BillFlowClassifier.isSale(null, TransactionType.OIL_SALE));
    }
}
