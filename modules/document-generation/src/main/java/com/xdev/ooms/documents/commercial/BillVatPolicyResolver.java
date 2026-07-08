package com.xdev.ooms.documents.commercial;

import com.xdev.ooms.sharedkernel.Enum.OperationType;
import com.xdev.ooms.sharedkernel.Enum.TransactionType;
import org.springframework.stereotype.Component;

@Component
public class BillVatPolicyResolver {

    private final PurchaseVatParameterReader purchaseVatParameterReader;

    public BillVatPolicyResolver(PurchaseVatParameterReader purchaseVatParameterReader) {
        this.purchaseVatParameterReader = purchaseVatParameterReader;
    }

    public BillVatMode resolve(OperationType operationType, TransactionType transactionType) {
        if (BillFlowClassifier.isSale(operationType, transactionType)) {
            return BillVatMode.NONE;
        }
        if (BillFlowClassifier.isPurchase(operationType, transactionType)) {
            return purchaseVatParameterReader.isAutoCalcEnabled()
                    ? BillVatMode.INCLUSIVE
                    : BillVatMode.STANDARD;
        }
        return BillVatMode.NONE;
    }
}
