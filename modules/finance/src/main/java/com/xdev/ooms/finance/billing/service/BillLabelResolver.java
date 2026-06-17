package com.xdev.ooms.finance.billing.service;

import com.xdev.ooms.sharedkernel.Enum.OperationType;
import com.xdev.ooms.sharedkernel.Enum.TransactionType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Human-readable bill labels aligned with frontend i18n
 * ({@code DELIVERIES.OPERATION_TYPE.*} and {@code TRANSACTIONS.TYPES.*}, French).
 */
@Component
public class BillLabelResolver {

    private static final Map<OperationType, String> OPERATION_LABELS = new EnumMap<>(OperationType.class);
    private static final Map<TransactionType, String> TRANSACTION_LABELS = new EnumMap<>(TransactionType.class);

    static {
        // DELIVERIES.OPERATION_TYPE + OPERATION_TYPE (fr.json)
        OPERATION_LABELS.put(OperationType.SIMPLE_RECEPTION, "Trituration particulier");
        OPERATION_LABELS.put(OperationType.BASE, "Base");
        OPERATION_LABELS.put(OperationType.EXCHANGE, "Echange");
        OPERATION_LABELS.put(OperationType.INTERNAL_RECEPTION, "réception interne");
        OPERATION_LABELS.put(OperationType.OIL_PURCHASE, "Achat d'huile");
        OPERATION_LABELS.put(OperationType.OLIVE_PURCHASE, "Achat d'olive");
        OPERATION_LABELS.put(OperationType.PAYMENT, "Paiement");
        OPERATION_LABELS.put(OperationType.OIL_SALE, "Vente d'Huile");
        OPERATION_LABELS.put(OperationType.WASTE_SALE, "Vente de déchets");
        OPERATION_LABELS.put(OperationType.OIL_CONTAINER_SALE, "Vente de contenants");
        OPERATION_LABELS.put(OperationType.OIL_SALE_PAYMENT, "Paiement vente huile");
        OPERATION_LABELS.put(OperationType.STORAGE_RENTAL, "Location de stockage");

        // TRANSACTIONS.TYPES (fr.json)
        TRANSACTION_LABELS.put(TransactionType.PAYMENT, "Paiement");
        TRANSACTION_LABELS.put(TransactionType.EXPENSE, "Dépense");
        TRANSACTION_LABELS.put(TransactionType.PURCHASE, "Achat");
        TRANSACTION_LABELS.put(TransactionType.CREDIT, "Crédit");
        TRANSACTION_LABELS.put(TransactionType.DEBIT, "Débit");
        TRANSACTION_LABELS.put(TransactionType.LOAN, "Prêt");
        TRANSACTION_LABELS.put(TransactionType.INTERNAL_TRANSFER, "Transfert Interne");
        TRANSACTION_LABELS.put(TransactionType.OIL_SALE, "Vente d'Huile");
        TRANSACTION_LABELS.put(TransactionType.OIL_PURCHASE, "Achat d'Huile");
        TRANSACTION_LABELS.put(TransactionType.OIL_CONTAINER_SALE, "Vente de contenants");
        TRANSACTION_LABELS.put(TransactionType.WASTE_SALE, "Vente de déchets");
        TRANSACTION_LABELS.put(TransactionType.WASTE_PAYMENT, "Paiement déchets");
        TRANSACTION_LABELS.put(TransactionType.WASTE_DISPOSAL_COST, "Coût élimination déchets");
        TRANSACTION_LABELS.put(TransactionType.SUPPLIER_PAYMENT, "Paiement agriculteur");
        TRANSACTION_LABELS.put(TransactionType.SUPPLIER_CREDIT, "Crédit Fournisseur");
        TRANSACTION_LABELS.put(TransactionType.STORAGE_RENTAL, "Location de stockage");
        TRANSACTION_LABELS.put(TransactionType.DEPOSIT, "Dépôt");
        TRANSACTION_LABELS.put(TransactionType.WITHDRAWAL, "Retrait");
        TRANSACTION_LABELS.put(TransactionType.CHECK_DEPOSIT, "Dépôt de Chèque");
        TRANSACTION_LABELS.put(TransactionType.CHECK_PAYMENT, "Paiement par Chèque");
        TRANSACTION_LABELS.put(TransactionType.RECEPTION_IN, "Réception");
        TRANSACTION_LABELS.put(TransactionType.TRANSFER_IN, "Transfert entrant");
        TRANSACTION_LABELS.put(TransactionType.FILTRATION, "Filtration");
        TRANSACTION_LABELS.put(TransactionType.SALE, "Vente");
        TRANSACTION_LABELS.put(TransactionType.EXCHANGE, "Échange");
    }

    public String operationTypeLabel(OperationType type) {
        if (type == null) {
            return null;
        }
        return OPERATION_LABELS.getOrDefault(type, type.name());
    }

    public String transactionTypeLabel(TransactionType type) {
        if (type == null) {
            return null;
        }
        return TRANSACTION_LABELS.getOrDefault(type, type.name());
    }

    public String resolveConditions(OperationType operationType, TransactionType transactionType) {
        if (operationType != null) {
            return operationTypeLabel(operationType);
        }
        if (transactionType != null) {
            return transactionTypeLabel(transactionType);
        }
        return null;
    }

    public String resolveDesignation(String description, OperationType operationType, TransactionType transactionType) {
        if (description != null && !description.isBlank()) {
            return description.trim();
        }
        String label = resolveConditions(operationType, transactionType);
        return label != null ? label : "Service";
    }
}
