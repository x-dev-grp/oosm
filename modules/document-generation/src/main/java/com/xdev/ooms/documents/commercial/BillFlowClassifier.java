package com.xdev.ooms.documents.commercial;

import com.xdev.ooms.sharedkernel.Enum.OperationType;
import com.xdev.ooms.sharedkernel.Enum.TransactionType;

/**
 * Classifies bill flows as purchase (buy) vs sale (sell) for VAT policy.
 */
public final class BillFlowClassifier {

    private BillFlowClassifier() {
    }

    public static boolean isSale(OperationType operationType, TransactionType transactionType) {
        if (operationType != null) {
            return switch (operationType) {
                case OIL_SALE, WASTE_SALE, OIL_CONTAINER_SALE, OIL_SALE_PAYMENT, STORAGE_RENTAL -> true;
                case OIL_PURCHASE, OLIVE_PURCHASE -> false;
                default -> false;
            };
        }
        if (transactionType != null) {
            return switch (transactionType) {
                case OIL_SALE, WASTE_SALE, OIL_CONTAINER_SALE, SALE, STORAGE_RENTAL, WASTE_PAYMENT -> true;
                case OIL_PURCHASE, PURCHASE, EXPENSE, SUPPLIER_PAYMENT, SUPPLIER_CREDIT -> false;
                default -> false;
            };
        }
        return false;
    }

    public static boolean isPurchase(OperationType operationType, TransactionType transactionType) {
        if (operationType != null) {
            return switch (operationType) {
                case OIL_PURCHASE, OLIVE_PURCHASE -> true;
                case OIL_SALE, WASTE_SALE, OIL_CONTAINER_SALE, OIL_SALE_PAYMENT, STORAGE_RENTAL -> false;
                default -> false;
            };
        }
        if (transactionType != null) {
            return switch (transactionType) {
                case OIL_PURCHASE, PURCHASE, EXPENSE, SUPPLIER_PAYMENT, SUPPLIER_CREDIT -> true;
                case OIL_SALE, WASTE_SALE, OIL_CONTAINER_SALE, SALE, STORAGE_RENTAL, WASTE_PAYMENT -> false;
                default -> false;
            };
        }
        return false;
    }
}
