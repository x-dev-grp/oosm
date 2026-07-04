package com.xdev.ooms.production.internal;

import com.xdev.ooms.production.oilsale.entity.OilSale;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.production.waste.entity.Waste;
import com.xdev.ooms.production.unifieddelivery.repository.DeliveryRepository;
import com.xdev.ooms.production.oilsale.repository.OilSaleRepository;
import com.xdev.ooms.production.waste.repository.WasteRepository;
import com.xdev.ooms.sharedkernel.Enum.DeliveryType;
import com.xdev.ooms.sharedkernel.Enum.ResourceName;
import com.xdev.ooms.sharedkernel.Enum.SaleStatus;
import com.xdev.ooms.sharedkernel.ports.ProductionFinancialSyncCommand;
import com.xdev.ooms.sharedkernel.ports.ProductionFinancialSyncPort;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ProductionFinancialSyncPortImpl implements ProductionFinancialSyncPort {

    private final DeliveryRepository deliveryRepository;
    private final OilSaleRepository oilSaleRepository;
    private final WasteRepository wasteRepository;

    public ProductionFinancialSyncPortImpl(
            DeliveryRepository deliveryRepository,
            OilSaleRepository oilSaleRepository,
            WasteRepository wasteRepository) {
        this.deliveryRepository = deliveryRepository;
        this.oilSaleRepository = oilSaleRepository;
        this.wasteRepository = wasteRepository;
    }

    @Override
    @Transactional
    public void syncFromFinancialTransaction(ProductionFinancialSyncCommand command) {
        if (command.resourceName() == null
                && command.externalTransactionId() == null
                && (command.lotNumber() == null || command.lotNumber().isBlank())) {
            return;
        }

        ResourceName resourceName = command.resourceName() != null
                ? command.resourceName()
                : ResourceName.UnifiedDelivery;

        switch (resourceName) {
            case UnifiedDelivery -> syncDelivery(command);
            case OILSALE -> syncOilSale(command);
            case Waste -> syncWaste(command);
            default -> OOSMLogger.logBusinessEvent(
                    this.getClass(),
                    "PRODUCTION_SYNC_SKIPPED",
                    "No sync handler for resource " + resourceName);
        }
    }

    private void syncDelivery(ProductionFinancialSyncCommand command) {
        resolveDelivery(command).ifPresentOrElse(delivery -> {
            applyDeliveryPayment(delivery, command.amount());
            deliveryRepository.save(delivery);
            OOSMLogger.logBusinessEvent(
                    this.getClass(),
                    "DELIVERY_PAYMENT_SYNCED",
                    "Synced delivery payment for lot " + delivery.getLotNumber()
                            + ", transaction type " + command.transactionType());
        }, () -> OOSMLogger.logBusinessEvent(
                this.getClass(),
                "DELIVERY_PAYMENT_SYNC_SKIPPED",
                "No delivery found for id=" + command.externalTransactionId()
                        + ", lotNumber=" + command.lotNumber()));
    }

    private Optional<UnifiedDelivery> resolveDelivery(ProductionFinancialSyncCommand command) {
        if (command.externalTransactionId() != null && !command.externalTransactionId().isBlank()) {
            UUID id = parseUuid(command.externalTransactionId());
            if (id != null) {
                Optional<UnifiedDelivery> byId =
                        deliveryRepository.findByIdAndIsDeletedFalse(id);
                if (byId.isPresent()) {
                    return byId;
                }
            }
        }

        if (command.lotNumber() != null && !command.lotNumber().isBlank()) {
            UnifiedDelivery oliveDelivery = deliveryRepository.findByLotNumberAndDeliveryType(
                    command.lotNumber(), DeliveryType.OLIVE);
            if (oliveDelivery != null && !Boolean.TRUE.equals(oliveDelivery.getDeleted())) {
                return Optional.of(oliveDelivery);
            }
            return deliveryRepository.findByLotNumberIn(Set.of(command.lotNumber())).stream()
                    .filter(delivery -> !Boolean.TRUE.equals(delivery.getDeleted()))
                    .findFirst();
        }

        return Optional.empty();
    }

    private void syncOilSale(ProductionFinancialSyncCommand command) {
        if (command.externalTransactionId() == null) {
            return;
        }
        UUID id = parseUuid(command.externalTransactionId());
        if (id == null) {
            return;
        }

        oilSaleRepository.findByIdAndIsDeletedFalse(id).ifPresent(sale -> {
            if (command.invoiceReference() != null && sale.getInvoiceNumber() == null) {
                sale.setInvoiceNumber(command.invoiceReference());
            }
            applyOilSalePayment(sale, command.amount());
            oilSaleRepository.save(sale);
        });
    }

    private void syncWaste(ProductionFinancialSyncCommand command) {
        if (command.externalTransactionId() == null) {
            return;
        }
        UUID id = parseUuid(command.externalTransactionId());
        if (id == null) {
            return;
        }

        wasteRepository.findByIdAndIsDeletedFalse(id).ifPresent(waste -> {
            if (command.invoiceReference() != null && waste.getInvoiceNumber() == null) {
                waste.setInvoiceNumber(command.invoiceReference());
            }
            applyWastePayment(waste, command.amount());
            wasteRepository.save(waste);
        });
    }

    private void applyDeliveryPayment(UnifiedDelivery delivery, BigDecimal paymentAmount) {
        if (paymentAmount == null || paymentAmount.signum() <= 0) {
            return;
        }
        double total = round3(safe(delivery.getPrice()));
        if (total <= 0.0) {
            total = round3(safe(delivery.getUnitPrice()) * safe(delivery.getOilQuantity()));
        }
        double paid = round3(safe(delivery.getPaidAmount()));
        paid = clamp(paid, 0.0, total);
        double unpaid = round3(Math.max(0.0, total - paid));
        double change = round3(Math.min(paymentAmount.doubleValue(), unpaid));
        double newPaid = round3(clamp(paid + change, 0.0, total));
        double newUnpaid = round3(Math.max(0.0, total - newPaid));
        delivery.setPrice(round3(total));
        delivery.setPaidAmount(newPaid);
        delivery.setUnpaidAmount(newUnpaid);
        delivery.setPaid(newUnpaid == 0.0);
    }

    private void applyOilSalePayment(OilSale sale, BigDecimal paymentAmount) {
        if (paymentAmount == null || paymentAmount.signum() <= 0) {
            return;
        }
        BigDecimal paidAmount = sale.getPaidAmount() != null
                ? BigDecimal.valueOf(sale.getPaidAmount())
                : BigDecimal.ZERO;
        BigDecimal unpaidAmount = sale.getUnpaidAmount() != null
                ? BigDecimal.valueOf(sale.getUnpaidAmount())
                : BigDecimal.ZERO;
        double payment = paymentAmount.doubleValue();
        sale.setPaidAmount(paidAmount.add(BigDecimal.valueOf(payment)).doubleValue());
        sale.setUnpaidAmount(unpaidAmount.subtract(BigDecimal.valueOf(payment)).max(BigDecimal.ZERO).doubleValue());
        sale.setPaid(sale.getUnpaidAmount() != null && sale.getUnpaidAmount() == 0.0);
        if (sale.isPaid()) {
            sale.setStatus(SaleStatus.DELIVERED);
        }
    }

    private void applyWastePayment(Waste waste, BigDecimal paymentAmount) {
        if (paymentAmount == null || paymentAmount.signum() <= 0) {
            return;
        }
        BigDecimal paidAmount = waste.getPaidAmount() != null
                ? BigDecimal.valueOf(waste.getPaidAmount())
                : BigDecimal.ZERO;
        BigDecimal unpaidAmount = waste.getUnpaidAmount() != null
                ? BigDecimal.valueOf(waste.getUnpaidAmount())
                : BigDecimal.ZERO;
        double payment = paymentAmount.doubleValue();
        waste.setPaidAmount(paidAmount.add(BigDecimal.valueOf(payment)).doubleValue());
        waste.setUnpaidAmount(unpaidAmount.subtract(BigDecimal.valueOf(payment)).max(BigDecimal.ZERO).doubleValue());
        waste.setPaid(waste.getUnpaidAmount() != null && waste.getUnpaidAmount() == 0.0);
    }

    private UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private double safe(Double value) {
        return value == null ? 0.0 : value;
    }

    private double round3(double value) {
        return BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
