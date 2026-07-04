package com.xdev.ooms.production.internal;

import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.production.unifieddelivery.repository.DeliveryRepository;
import com.xdev.ooms.sharedkernel.Enum.DeliveryType;
import com.xdev.ooms.sharedkernel.Enum.OperationType;
import com.xdev.ooms.sharedkernel.ports.UnifiedDeliveryBillLineDto;
import com.xdev.ooms.sharedkernel.ports.UnifiedDeliveryBillLinePort;
import com.xdev.ooms.sharedkernel.ports.UnifiedDeliveryBillLineQuery;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class UnifiedDeliveryBillLinePortImpl implements UnifiedDeliveryBillLinePort {

    private final DeliveryRepository deliveryRepository;

    public UnifiedDeliveryBillLinePortImpl(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    @Override
    public Optional<UnifiedDeliveryBillLineDto> resolve(UnifiedDeliveryBillLineQuery query) {
        if (query == null) {
            return Optional.empty();
        }
        return findDelivery(query).map(delivery -> toDto(delivery, query.fallbackTotalHt()));
    }

    private UnifiedDeliveryBillLineDto toDto(UnifiedDelivery delivery, BigDecimal fallbackTotalHt) {
        boolean isOil = delivery.getDeliveryType() == DeliveryType.OIL;
        BigDecimal quantity = isOil ? oilQuantity(delivery) : oliveQuantity(delivery);
        String unit = isOil ? "L" : "kg";

        BigDecimal totalHt = money(delivery.getPrice());
        if (totalHt.signum() <= 0 && fallbackTotalHt != null) {
            totalHt = money(fallbackTotalHt);
        }

        BigDecimal unitPrice = money(delivery.getUnitPrice());
        if (unitPrice.signum() <= 0 && totalHt.signum() > 0 && quantity.signum() > 0) {
            unitPrice = totalHt.divide(quantity, 8, RoundingMode.HALF_UP);
        }
        if (quantity.signum() <= 0 && unitPrice.signum() > 0 && totalHt.signum() > 0) {
            quantity = totalHt.divide(unitPrice, 3, RoundingMode.HALF_UP);
        }

        return new UnifiedDeliveryBillLineDto(
                quantity,
                unit,
                unitPrice,
                totalHt,
                delivery.getOperationType(),
                isOil);
    }

    private Optional<UnifiedDelivery> findDelivery(UnifiedDeliveryBillLineQuery query) {
        Optional<UnifiedDelivery> byExternal = resolveByExternalTransactionId(query.externalTransactionId());
        if (byExternal.isPresent()) {
            return byExternal;
        }

        String lot = query.lotNumber();
        if (lot == null || lot.isBlank()) {
            return Optional.empty();
        }

        return resolveByLotNumber(lot.trim(), query.operationType());
    }

    private Optional<UnifiedDelivery> resolveByExternalTransactionId(String externalTransactionId) {
        if (externalTransactionId == null || externalTransactionId.isBlank()) {
            return Optional.empty();
        }
        try {
            UUID uuid = UUID.fromString(externalTransactionId.trim());
            return deliveryRepository.findByIdAndIsDeletedFalse(uuid);
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    private Optional<UnifiedDelivery> resolveByLotNumber(String lot, OperationType operationType) {
        if (operationType == OperationType.OIL_PURCHASE) {
            UnifiedDelivery oil = deliveryRepository.findByLotNumberAndDeliveryType(lot, DeliveryType.OIL);
            if (oil != null && !Boolean.TRUE.equals(oil.getDeleted())) {
                return Optional.of(oil);
            }
        } else {
            UnifiedDelivery olive = deliveryRepository.findByLotNumberAndDeliveryType(lot, DeliveryType.OLIVE);
            if (olive != null && !Boolean.TRUE.equals(olive.getDeleted())) {
                return Optional.of(olive);
            }
        }

        List<UnifiedDelivery> byLotNumber = deliveryRepository.findByLotNumberIn(Set.of(lot)).stream()
                .filter(d -> !Boolean.TRUE.equals(d.getDeleted()))
                .sorted(Comparator.comparing(UnifiedDelivery::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
        Optional<UnifiedDelivery> fromLot = pickDelivery(byLotNumber, operationType);
        if (fromLot.isPresent()) {
            return fromLot;
        }

        List<UnifiedDelivery> byGlobalLot = deliveryRepository.findByGlobalLotNumber(lot).stream()
                .filter(d -> !Boolean.TRUE.equals(d.getDeleted()))
                .sorted(Comparator.comparing(UnifiedDelivery::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
        return pickDelivery(byGlobalLot, operationType);
    }

    private Optional<UnifiedDelivery> pickDelivery(List<UnifiedDelivery> deliveries, OperationType operationType) {
        if (deliveries.isEmpty()) {
            return Optional.empty();
        }
        if (operationType == OperationType.OIL_PURCHASE) {
            Optional<UnifiedDelivery> oil = deliveries.stream()
                    .filter(d -> d.getDeliveryType() == DeliveryType.OIL)
                    .findFirst();
            if (oil.isPresent()) {
                return oil;
            }
        }
        return deliveries.stream()
                .filter(d -> d.getDeliveryType() == DeliveryType.OLIVE)
                .findFirst()
                .or(() -> deliveries.stream().findFirst());
    }

    private BigDecimal oliveQuantity(UnifiedDelivery delivery) {
        BigDecimal net = money(delivery.getPoidsNet());
        if (net.signum() > 0) {
            return net;
        }
        return money(delivery.getOliveQuantity());
    }

    private BigDecimal oilQuantity(UnifiedDelivery delivery) {
        BigDecimal oil = money(delivery.getOilQuantity());
        if (oil.signum() > 0) {
            return oil;
        }
        return money(delivery.getPoidsNet());
    }

    private BigDecimal money(Double value) {
        return value == null ? BigDecimal.ZERO : BigDecimal.valueOf(value);
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
