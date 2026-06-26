package com.xdev.ooms.production.oiltransaction.service;


import com.xdev.ooms.production.oilsale.entity.OilSale;



import com.xdev.ooms.production.unifieddelivery.dto.ExchangePricingDto;
import com.xdev.ooms.production.oiltransaction.dto.OilTransactionDTO;
import com.xdev.ooms.production.oiltransaction.entity.OilTransaction;
import com.xdev.ooms.production.storageunit.entity.StorageUnit;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.production.unifieddelivery.repository.DeliveryRepository;
import com.xdev.ooms.production.oiltransaction.repository.OilTransactionRepository;
import com.xdev.ooms.production.storageunit.repository.StorageUnitRepo;
import  com.xdev.ooms.sharedkernel.Enum.DeliveryType;
import  com.xdev.ooms.sharedkernel.Enum.OliveLotStatus;
import  com.xdev.ooms.sharedkernel.Enum.TransactionState;
import  com.xdev.ooms.sharedkernel.Enum.TransactionType;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.ports.OilCreditPort;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static  com.xdev.ooms.sharedkernel.Enum.OperationType.EXCHANGE;

/**
 * Service class for managing oil transactions, including creation, approval, and action mapping.
 * Handles business logic for different transaction types and states, and integrates with storage and credit services.
 */
@Service
@Transactional
public class OilTransactionService extends BaseServiceImpl<OilTransaction, OilTransactionDTO, OilTransactionDTO> {
    private final OilTransactionRepository oilTransactionRepository;
    private final StorageUnitRepo storageUnitRepo;
    private final OilCreditPort oilCreditPort;
    private final DeliveryRepository unifiedDeliveryRepo;
    private final DeliveryRepository deliveryRepository;

    /**
     * Constructs the OilTransactionService with required dependencies.
     *
     * @param repository           OilTransactionRepository for persistence
     * @param modelMapper          ModelMapper for DTO/entity conversion
     * @param storageUnitRepo      StorageUnitRepo for storage unit persistence
     * @param oilCreditPort        Local port for oil credit operations
     */
    public OilTransactionService(OilTransactionRepository repository, ModelMapper modelMapper, StorageUnitRepo storageUnitRepo, OilCreditPort oilCreditPort, DeliveryRepository unifiedDeliveryRepo, DeliveryRepository deliveryRepository) {
        super(repository, modelMapper);
        this.oilTransactionRepository = repository;
        this.storageUnitRepo = storageUnitRepo;
        this.oilCreditPort = oilCreditPort;
        this.unifiedDeliveryRepo = unifiedDeliveryRepo;
        this.deliveryRepository = deliveryRepository;
    }

    /**
     * Helper to create an OilTransaction for an exchange operation.
     *
     * @param delivery UnifiedDelivery object
     * @param dto      ExchangePricingDto with pricing details
     * @return OilTransaction entity
     */
    private static OilTransaction getOilTransaction(UnifiedDelivery delivery, ExchangePricingDto dto) {
        OOSMLogger.logMethodEntry(OilTransactionService.class, "getOilTransaction", delivery);
        OilTransaction tx = new OilTransaction();
        tx.setStorageUnitDestination(null);
        tx.setStorageUnitSource(null);
        tx.setTransactionType(TransactionType.EXCHANGE);
        tx.setTransactionState(TransactionState.PENDING);
        tx.setTotalPrice(dto.getOilTotalValue());
        tx.setReception(delivery);
        tx.setUnitPrice(dto.getOilUnitPrice());
        tx.setQualityGrade(dto.getQualityGrade());
        tx.setOilType(delivery.getOilType());
        tx.setQuantityKg(dto.getOilQuantity());
        OOSMLogger.logMethodExit(OilTransactionService.class, "getOilTransaction", tx);
        return tx;
    }

    private static OilTransaction getOilTransaction(UnifiedDelivery delivery) {
        OilTransaction tx = new OilTransaction();
        tx.setStorageUnitDestination(delivery.getStorageUnit());
        tx.setStorageUnitSource(null);
        tx.setTransactionType(TransactionType.RECEPTION_IN);
        tx.setTransactionState(TransactionState.COMPLETED);
        tx.setQuantityKg(delivery.getOilQuantity());
        tx.setQualityGrade(delivery.getCategoryOliveOil());
        tx.setUnitPrice(delivery.getUnitPrice());
        tx.setTotalPrice(delivery.getUnitPrice() * delivery.getOilQuantity());
        tx.setReception(delivery);
        tx.setOilType(delivery.getOilType());
        return tx;
    }

    /**
     * Reverses an oil transaction associated with a given OilSale ID.
     *
     * @param oilSaleId UUID of the OilSale to reverse
     * @throws IllegalArgumentException if the transaction is not found or already reversed
     * @throws RuntimeException         if there’s an error during reversal
     */
    @Transactional
    public void reverseOilTransactionForSale(UUID oilSaleId) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "reverseOilTransactionForSale", oilSaleId);

        try {
            // 1. Find the oil transaction
            OilTransaction oilTransaction = oilTransactionRepository.findByOilSaleIdAndIsDeletedFalse(oilSaleId).orElseThrow(() -> new IllegalArgumentException("No oil transaction found for OilSale ID: " + oilSaleId));

            // 2. Validate transaction state
            if (oilTransaction.getTransactionState() == TransactionState.CANCELED) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.WARN, "Oil transaction for OilSale %s is already canceled", oilSaleId);
                throw new IllegalStateException("Oil transaction is already canceled for OilSale: " + oilSaleId);
            }

            // 3. Restore oil quantity to source storage unit
            if (oilTransaction.getStorageUnitSource() != null) {
                StorageUnit source = storageUnitRepo.findByIdAndIsDeletedFalse(oilTransaction.getStorageUnitSource().getId()).orElseThrow(() -> new IllegalArgumentException("Source storage unit not found: " + oilTransaction.getStorageUnitSource().getId()));
                source.updateCurrentVolume(oilTransaction.getQuantityKg(), 1, oilTransaction.getUnitPrice());
                storageUnitRepo.save(source);
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "Restored %.2f kg to storage unit %s", oilTransaction.getQuantityKg(), source.getId());
            } else {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.WARN, "No source storage unit found for oil transaction %s", oilTransaction.getId());
            }

            // 4. Mark transaction as canceled or deleted
            oilTransaction.setTransactionState(TransactionState.CANCELED);
            oilTransaction.setDeleted(true); // Soft-delete
            oilTransactionRepository.save(oilTransaction);
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "Oil transaction %s canceled for OilSale %s", oilTransaction.getId(), oilSaleId);

        } catch (IllegalArgumentException e) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "Validation error in reverseOilTransactionForSale: %s", e.getMessage());
            throw e;
        } catch (Exception e) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "Unexpected error in reverseOilTransactionForSale: %s", e.getMessage(), e);
            throw new RuntimeException("Failed to reverse oil transaction for OilSale: " + oilSaleId, e);
        }

        OOSMLogger.logMethodExit(this.getClass(), "reverseOilTransactionForSale", null);
        OOSMLogger.logPerformance(this.getClass(), "reverseOilTransactionForSale", startTime, System.currentTimeMillis());
    }

    /**
     * Saves a new or updated oil transaction, updating storage units as needed.
     *
     * @param request OilTransactionDTO to save
     * @return Saved OilTransactionDTO
     */
    @Override
    public OilTransactionDTO save(OilTransactionDTO request) {
        java.util.function.Function<Double, Double> rd = v -> BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "save", request);
        OilTransaction oilTransaction = modelMapper.map(request, OilTransaction.class);
        boolean isTransfertIN = oilTransaction.getTransactionType() == TransactionType.TRANSFER_IN;
        if (request.getStorageUnitSource() != null && request.getStorageUnitSource().getId() != null) {

            StorageUnit src = storageUnitRepo.findById(request.getStorageUnitSource().getId()).orElse(null);
            oilTransaction.setStorageUnitSource(src);
            if (isTransfertIN) {
                oilTransaction.setUnitPrice(src.getAvgCost());
                oilTransaction.setTransactionState(TransactionState.COMPLETED);
            }
        }
        // Always fetch and set StorageUnit entities by ID to avoid natural identifier errors
        if (request.getStorageUnitDestination() != null && request.getStorageUnitDestination().getId() != null) {
            StorageUnit dest = storageUnitRepo.findById(request.getStorageUnitDestination().getId()).orElse(null);
            oilTransaction.setStorageUnitDestination(dest);
        }


        oilTransaction.setTotalPrice();
        oilTransaction = oilTransactionRepository.save(oilTransaction);
        StorageUnit storageUnitDestination = oilTransaction.getStorageUnitDestination();
        StorageUnit storageUnitSource = oilTransaction.getStorageUnitSource();
        validateNonNegativeVolumeBeforeSave(storageUnitSource, storageUnitDestination, oilTransaction.getQuantityKg());
        // Update destination storage unit if present
        if (storageUnitDestination != null) {
            storageUnitDestination.updateCurrentVolume(oilTransaction.getQuantityKg(), 1, oilTransaction.getUnitPrice());

            storageUnitRepo.save(storageUnitDestination);
        }
        // Update source storage unit if present
        if (storageUnitSource != null) {
            storageUnitSource.updateCurrentVolume(oilTransaction.getQuantityKg(), 0, null);

            storageUnitRepo.save(storageUnitSource);
        }


        OOSMLogger.logMethodExit(this.getClass(), "save", modelMapper.map(oilTransaction, OilTransactionDTO.class));
        OOSMLogger.logPerformance(this.getClass(), "save", startTime, System.currentTimeMillis());
        return modelMapper.map(oilTransaction, OilTransactionDTO.class);
    }

    /**
     * Saves an oil transaction record without applying any source/destination
     * stock movement.
     * Use this when stock was already adjusted by the caller in the same flow
     * (e.g. filtration completion).
     */
    public OilTransactionDTO saveWithoutStockAdjustment(OilTransactionDTO request) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "saveWithoutStockAdjustment", request);

        OilTransaction oilTransaction = modelMapper.map(request, OilTransaction.class);

        if (request.getStorageUnitSource() != null && request.getStorageUnitSource().getId() != null) {
            StorageUnit src = storageUnitRepo.findById(request.getStorageUnitSource().getId()).orElse(null);
            oilTransaction.setStorageUnitSource(src);
        }

        if (request.getStorageUnitDestination() != null && request.getStorageUnitDestination().getId() != null) {
            StorageUnit dest = storageUnitRepo.findById(request.getStorageUnitDestination().getId()).orElse(null);
            oilTransaction.setStorageUnitDestination(dest);
        }

        oilTransaction.setTotalPrice();
        oilTransaction = oilTransactionRepository.save(oilTransaction);

        OOSMLogger.logMethodExit(this.getClass(), "saveWithoutStockAdjustment", oilTransaction);
        OOSMLogger.logPerformance(this.getClass(), "saveWithoutStockAdjustment", startTime, System.currentTimeMillis());
        return modelMapper.map(oilTransaction, OilTransactionDTO.class);
    }

    @Override
    @Transactional
    public OilTransactionDTO delete(UUID id) {
        OOSMLogger.logMethodEntry(this.getClass(), "delete", id);
        try {
            if (id == null) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.WARN, "Delete ID is null: {}", id);
                return null;
            }
            OilTransaction entity = repository.findByIdAndIsDeletedFalse(id).orElse(null);
            if (entity == null) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.WARN, "Entity with ID {} not found for deletion", id);
                return null;
            }
            entity.setDeleted(true);
            OilTransaction updatedEntity = repository.save(entity);
            OilTransactionDTO result = modelMapper.map(updatedEntity, outDTOClass);
            if (updatedEntity.getStorageUnitSource() != null) {
                StorageUnit storageUnitSource = updatedEntity.getStorageUnitSource();
                storageUnitSource.updateDeletedCurrentVolume(updatedEntity.getQuantityKg(), 0, null);
                storageUnitRepo.save(storageUnitSource);

            }
            if (updatedEntity.getStorageUnitDestination() != null) {
                StorageUnit storageUnitDestination = updatedEntity.getStorageUnitDestination();
                validateReverseNonNegativeVolumeBeforeSave(storageUnitDestination, updatedEntity.getQuantityKg());
                storageUnitDestination.updateDeletedCurrentVolume(updatedEntity.getQuantityKg(), 1, updatedEntity.getUnitPrice());
                storageUnitRepo.save(storageUnitDestination);

            }
            return result;
        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Error deleting entity with ID: " + id, e);
            throw e;
        }
    }


    /**
     * Approves an oil transaction by applying business logic based on its type.
     *
     * @param dto OilTransactionDTO to approve
     * @return Approved OilTransactionDTO
     */
    public OilTransactionDTO approveOilTransaction2(OilTransactionDTO dto) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "approveOilTransaction2", dto);
        if (dto == null || dto.getId() == null) {
            OOSMLogger.logMethodExit(this.getClass(), "approveOilTransaction2", null);
            OOSMLogger.logPerformance(this.getClass(), "approveOilTransaction2", startTime, System.currentTimeMillis());
            return null;
        }
        OilTransaction oilTransaction = oilTransactionRepository.findById(dto.getId()).orElseThrow(() -> new RuntimeException("Oil transaction not found"));
        handleApprovalLogicByType(oilTransaction, dto);
        OOSMLogger.logMethodExit(this.getClass(), "approveOilTransaction2", modelMapper.map(oilTransaction, OilTransactionDTO.class));
        OOSMLogger.logPerformance(this.getClass(), "approveOilTransaction2", startTime, System.currentTimeMillis());
        return modelMapper.map(oilTransaction, OilTransactionDTO.class);
    }

    /**
     * Handles approval logic for different transaction types.
     *
     * @param oilTransaction OilTransaction entity
     * @param dto            OilTransactionDTO with approval data
     */
    private void handleApprovalLogicByType(OilTransaction oilTransaction, OilTransactionDTO dto) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "handleApprovalLogicByType", oilTransaction);
        TransactionType type = oilTransaction.getTransactionType();
        // Dispatch to the correct handler based on transaction type
        switch (type) {
            case RECEPTION_IN -> handleReceptionIn(oilTransaction, dto);
            case TRANSFER_IN -> handleTransferIn(oilTransaction, dto);
            case OIL_SALE -> handleSale(oilTransaction, dto);
            case LOAN -> handleLoan(oilTransaction, dto);
            case EXCHANGE -> handleExchange(oilTransaction, dto);
            default -> throw new IllegalArgumentException("Unsupported transaction type: " + type);
        }

        // Save the updated transaction
        save(modelMapper.map(oilTransaction, OilTransactionDTO.class));
        OOSMLogger.logMethodExit(this.getClass(), "handleApprovalLogicByType", oilTransaction);
        OOSMLogger.logPerformance(this.getClass(), "handleApprovalLogicByType", startTime, System.currentTimeMillis());
    }

    /**
     * Handles approval for RECEPTION_IN transactions.
     */
    private void handleReceptionIn(OilTransaction oilTransaction, OilTransactionDTO dto) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "handleReceptionIn", oilTransaction);
        if (dto.getStorageUnitDestination() != null) {
            StorageUnit dest = storageUnitRepo.findById(dto.getStorageUnitDestination().getId()).orElseThrow();
            oilTransaction.setStorageUnitDestination(dest);
            oilTransaction.setTransactionState(TransactionState.COMPLETED);
        }
        if (oilTransaction.getReception() != null && oilTransaction.getReception().getId() != null) {
            UUID reception = oilTransaction.getReception().getId();
            UnifiedDelivery unifiedDelivery = unifiedDeliveryRepo.findById(reception).orElse(null);
            if (unifiedDelivery != null) {
                unifiedDelivery.setStatus(OliveLotStatus.IN_STOCK);
                unifiedDeliveryRepo.save(unifiedDelivery);
            }

        }
        OOSMLogger.logMethodExit(this.getClass(), "handleReceptionIn", oilTransaction);
        OOSMLogger.logPerformance(this.getClass(), "handleReceptionIn", startTime, System.currentTimeMillis());
    }

    /**
     * Handles approval for TRANSFER_IN transactions.
     */
    private void handleTransferIn(OilTransaction oilTransaction, OilTransactionDTO dto) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "handleTransferIn", oilTransaction);
        if (dto.getStorageUnitSource() != null && dto.getStorageUnitDestination() != null) {
            StorageUnit source = storageUnitRepo.findById(dto.getStorageUnitSource().getId()).orElseThrow();
            StorageUnit dest = storageUnitRepo.findById(dto.getStorageUnitDestination().getId()).orElseThrow();
            oilTransaction.setStorageUnitSource(source);
            oilTransaction.setStorageUnitDestination(dest);
            oilTransaction.setTransactionState(TransactionState.COMPLETED);
        }

        OOSMLogger.logMethodExit(this.getClass(), "handleTransferIn", oilTransaction);
        OOSMLogger.logPerformance(this.getClass(), "handleTransferIn", startTime, System.currentTimeMillis());
    }

    /**
     * Handles approval for SALE transactions.
     */
    private void handleSale(OilTransaction oilTransaction, OilTransactionDTO dto) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "handleSale", oilTransaction);
        if (dto.getStorageUnitSource() != null) {
            StorageUnit source = storageUnitRepo.findById(dto.getStorageUnitSource().getId()).orElseThrow();
            oilTransaction.setStorageUnitSource(source);
            oilTransaction.setTotalPrice();
            oilTransaction.setTransactionState(TransactionState.COMPLETED);
        }
        OOSMLogger.logMethodExit(this.getClass(), "handleSale", oilTransaction);
        OOSMLogger.logPerformance(this.getClass(), "handleSale", startTime, System.currentTimeMillis());
    }

    /**
     * Handles approval for LOAN transactions.
     */
    private void handleLoan(OilTransaction oilTransaction, OilTransactionDTO dto) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "handleLoan", oilTransaction);
        if (dto.getStorageUnitSource() != null) {
            StorageUnit source = storageUnitRepo.findById(dto.getStorageUnitSource().getId()).orElseThrow();
            oilTransaction.setStorageUnitSource(source);
            oilTransaction.setUnitPrice(source.getAvgCost());
            oilTransaction.setTotalPrice();
            oilTransaction.setTransactionState(TransactionState.COMPLETED);
            oilCreditPort.approveOilCredit(oilTransaction.getExternalId());
        }
        OOSMLogger.logMethodExit(this.getClass(), "handleLoan", oilTransaction);
        OOSMLogger.logPerformance(this.getClass(), "handleLoan", startTime, System.currentTimeMillis());
    }

    /**
     * Handles approval for EXCHANGE transactions.
     */
    private void handleExchange(OilTransaction oilTransaction, OilTransactionDTO dto) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "handleExchange", oilTransaction);
        if (dto.getStorageUnitSource() != null) {
            StorageUnit source = storageUnitRepo.findById(dto.getStorageUnitSource().getId()).orElseThrow();
            oilTransaction.setStorageUnitSource(source);
//            oilTransaction.setUnitPrice(source.getAvgCost());
//            oilTransaction.setTotalPrice();
            oilTransaction.setTransactionState(TransactionState.COMPLETED);
            UUID reception = oilTransaction.getReception().getId();
            UnifiedDelivery unifiedDelivery = unifiedDeliveryRepo.findById(reception).orElse(null);
            if (unifiedDelivery != null) {
                unifiedDelivery.setPaid(true);
                unifiedDelivery.setUnpaidAmount(0.0);
                unifiedDeliveryRepo.save(unifiedDelivery);
            }
        }
        if (oilTransaction.getReception() != null && oilTransaction.getReception().getId() != null) {
            UUID reception = oilTransaction.getReception().getId();
            UnifiedDelivery unifiedDelivery = unifiedDeliveryRepo.findById(reception).orElse(null);
            if (unifiedDelivery != null) {
                unifiedDelivery.setStatus(OliveLotStatus.PROD_READY);
                unifiedDeliveryRepo.save(unifiedDelivery);
            }
        }
        OOSMLogger.logMethodExit(this.getClass(), "handleExchange", oilTransaction);
        OOSMLogger.logPerformance(this.getClass(), "handleExchange", startTime, System.currentTimeMillis());
    }

    /**
     * Finds all oil transactions for a given storage unit ID.
     *
     * @param storageUnitId UUID of the storage unit
     * @return List of OilTransaction entities
     */
    public List<OilTransaction> findByStorageUnitId(UUID storageUnitId) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "findByStorageUnitId", storageUnitId);
        List<OilTransaction> transactions = oilTransactionRepository.findByStorageUnitDestinationId(storageUnitId);
        OOSMLogger.logMethodExit(this.getClass(), "findByStorageUnitId", transactions);
        OOSMLogger.logPerformance(this.getClass(), "findByStorageUnitId", startTime, System.currentTimeMillis());
        return transactions;
    }

    /**
     * Maps available actions for an oil transaction based on its state.
     *
     * @param oilTransaction OilTransaction entity
     * @return Set of allowed Action enums
     */

    @Override
    public Set<Action> actionsMapping(OilTransaction oilTransaction) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "actionsMapping", oilTransaction);
        Set<Action> actions = new HashSet<>();
        actions.add(Action.READ);
        // Only allow update/delete/validate if transaction is pending
        switch (oilTransaction.getTransactionState()) {
            case PENDING -> {
                actions.add(Action.UPDATE);
                actions.add(Action.DELETE);
                actions.add(Action.VALIDATE);
            }
            case COMPLETED -> {
                actions.add(Action.GEN_PDF);
                actions.add(Action.DELETE);
            }
            case null, default -> {
            }
        }
        OOSMLogger.logMethodExit(this.getClass(), "actionsMapping", actions);
        OOSMLogger.logPerformance(this.getClass(), "actionsMapping", startTime, System.currentTimeMillis());
        return actions;
    }

    /**
     * Creates a single oil transaction for a delivery (reception in).
     * This method validates the delivery and creates a corresponding oil transaction.
     *
     * @param delivery UnifiedDelivery entity
     * @throws IllegalArgumentException if delivery is null or invalid
     * @throws RuntimeException         if there's an error during processing
     */
    @Transactional
    public void createSingleOilTransactionIn(UnifiedDelivery delivery) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "createSingleOilTransactionIn", delivery);

        // Validate input parameters
        if (delivery == null) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[createSingleOilTransactionIn] Delivery is null");
            throw new IllegalArgumentException("Delivery cannot be null");
        }

        OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[createSingleOilTransactionIn] Creating oil transaction for delivery %s (Type: %s, Status: %s)", delivery.getLotNumber(), delivery.getDeliveryType(), delivery.getStatus());

        try {
            // Validate delivery type
            if (delivery.getDeliveryType() != DeliveryType.OIL) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[createSingleOilTransactionIn] Invalid delivery type for oil transaction: %s (expected OIL)", delivery.getDeliveryType());
                throw new IllegalArgumentException("Oil transaction can only be created for OIL deliveries");
            }

            // Validate delivery state
            if (delivery.getStatus() == OliveLotStatus.IN_STOCK) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.WARN, "[createSingleOilTransactionIn] Creating oil transaction for delivery %s that is already IN_STOCK", delivery.getLotNumber());
            }

            // Validate required fields
            if (delivery.getOilQuantity() == null || delivery.getOilQuantity() <= 0) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[createSingleOilTransactionIn] Invalid oil quantity for delivery %s: %s", delivery.getLotNumber(), delivery.getOilQuantity());
                throw new IllegalArgumentException("Oil quantity must be positive for oil transaction creation");
            }

            if (delivery.getUnitPrice() == null || delivery.getUnitPrice() <= 0) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[createSingleOilTransactionIn] Invalid unit price for delivery %s: %s", delivery.getLotNumber(), delivery.getUnitPrice());
                throw new IllegalArgumentException("Unit price must be positive for oil transaction creation");
            }

            // Create oil transaction
            OilTransaction tx = getOilTransaction(delivery);
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[createSingleOilTransactionIn] Created oil transaction for delivery %s with quantity %.2f and unit price %.2f", delivery.getLotNumber(), tx.getQuantityKg(), tx.getUnitPrice());

            // Save the transaction
            OilTransactionDTO savedTx = save(modelMapper.map(tx, OilTransactionDTO.class));


            deliveryRepository.save(delivery);
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[createSingleOilTransactionIn] Successfully saved oil transaction %s for delivery %s", savedTx.getId(), delivery.getLotNumber());

        } catch (IllegalArgumentException e) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[createSingleOilTransactionIn] Validation error: %s", e.getMessage());
            throw e;
        } catch (Exception e) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[createSingleOilTransactionIn] Unexpected error during oil transaction creation: %s", e.getMessage(), e);
            throw new RuntimeException("Failed to create oil transaction", e);
        }

        OOSMLogger.logMethodExit(this.getClass(), "createSingleOilTransactionIn", null);
        OOSMLogger.logPerformance(this.getClass(), "createSingleOilTransactionIn", startTime, System.currentTimeMillis());
    }

    /**
     * Creates a single oil transaction for an exchange out operation.
     * This method validates the delivery and pricing data, then creates a corresponding oil transaction.
     *
     * @param delivery UnifiedDelivery entity
     * @param dto      ExchangePricingDto with pricing details
     * @throws IllegalArgumentException if delivery or dto is null or invalid
     * @throws RuntimeException         if there's an error during processing
     */
    @Transactional
    public void createSingleOilTransactionOut(UnifiedDelivery delivery, ExchangePricingDto dto) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "createSingleOilTransactionOut", String.format("delivery=%s, dto=%s", delivery != null ? delivery.getLotNumber() : "null", dto));

        // Validate input parameters
        if (delivery == null) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[createSingleOilTransactionOut] Delivery is null");
            throw new IllegalArgumentException("Delivery cannot be null");
        }

        if (dto == null) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[createSingleOilTransactionOut] ExchangePricingDto is null");
            throw new IllegalArgumentException("ExchangePricingDto cannot be null");
        }

        OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[createSingleOilTransactionOut] Creating oil transaction out for delivery %s (Type: %s, Status: %s, Operation: %s)", delivery.getLotNumber(), delivery.getDeliveryType(), delivery.getStatus(), delivery.getOperationType());

        try {


            // Validate operation type for exchange
            if (delivery.getOperationType() != EXCHANGE) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.WARN, "[createSingleOilTransactionOut] Creating oil transaction out for non-exchange operation: %s", delivery.getOperationType());
            }

            // Validate delivery state
            if (delivery.getStatus() == OliveLotStatus.IN_STOCK) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.WARN, "[createSingleOilTransactionOut] Creating oil transaction out for delivery %s that is already IN_STOCK", delivery.getLotNumber());
            }

            // Validate pricing data
            if (dto.getOilQuantity() == null || dto.getOilQuantity() <= 0) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[createSingleOilTransactionOut] Invalid oil quantity in DTO for delivery %s: %s", delivery.getLotNumber(), dto.getOilQuantity());
                throw new IllegalArgumentException("Oil quantity must be positive for oil transaction out creation");
            }

            if (dto.getOilUnitPrice() == null || dto.getOilUnitPrice() <= 0) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[createSingleOilTransactionOut] Invalid oil unit price in DTO for delivery %s: %s", delivery.getLotNumber(), dto.getOilUnitPrice());
                throw new IllegalArgumentException("Oil unit price must be positive for oil transaction out creation");
            }

            if (dto.getOilTotalValue() == null || dto.getOilTotalValue() <= 0) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[createSingleOilTransactionOut] Invalid oil total value in DTO for delivery %s: %s", delivery.getLotNumber(), dto.getOilTotalValue());
                throw new IllegalArgumentException("Oil total value must be positive for oil transaction out creation");
            }

            // Create oil transaction
            OilTransaction tx = getOilTransaction(delivery, dto);
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[createSingleOilTransactionOut] Created oil transaction out for delivery %s with quantity %.2f, unit price %.2f, total value %.2f", delivery.getLotNumber(), tx.getQuantityKg(), tx.getUnitPrice(), tx.getTotalPrice());

            // Save the transaction
            OilTransactionDTO savedTx = save(modelMapper.map(tx, OilTransactionDTO.class));
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[createSingleOilTransactionOut] Successfully saved oil transaction out %s for delivery %s", savedTx.getId(), delivery.getLotNumber());

        } catch (IllegalArgumentException e) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[createSingleOilTransactionOut] Validation error: %s", e.getMessage());
            throw e;
        } catch (Exception e) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[createSingleOilTransactionOut] Unexpected error during oil transaction out creation: %s", e.getMessage(), e);
            throw new RuntimeException("Failed to create oil transaction out", e);
        }

        OOSMLogger.logMethodExit(this.getClass(), "createSingleOilTransactionOut", null);
        OOSMLogger.logPerformance(this.getClass(), "createSingleOilTransactionOut", startTime, System.currentTimeMillis());
    }

    @Transactional
    public OilTransactionDTO createOilTransactionForSale(OilTransactionDTO oilTransactionDTO) {
        OilTransactionDTO oilTransactionDTOforSale = modelMapper.map(oilTransactionDTO, OilTransactionDTO.class);
        oilTransactionDTOforSale.setTransactionType(TransactionType.OIL_SALE);
        oilTransactionDTOforSale.setTransactionState(TransactionState.PENDING);
        // Stock is deducted when storage validates/approves the linked transaction, not at sale create.
        return saveWithoutStockAdjustment(oilTransactionDTOforSale);
    }

    private void validateNonNegativeVolumeBeforeSave(StorageUnit source, StorageUnit destination, Double quantityKg) {
        if (quantityKg == null || quantityKg <= 0) {
            throw new IllegalArgumentException("Quantite d'huile invalide: elle doit etre strictement positive.");
        }

        if (source != null) {
            double sourceVolume = source.getCurrentVolume() == null ? 0d : source.getCurrentVolume();
            double projectedSource = sourceVolume - quantityKg;
            if (projectedSource < 0) {
                throw new IllegalStateException(String.format(
                        "Operation refusee: volume negatif detecte sur la cuve source (%s). Disponible=%.3f, requis=%.3f",
                        source.getName(), sourceVolume, quantityKg));
            }
        }

        if (destination != null) {
            double destinationVolume = destination.getCurrentVolume() == null ? 0d : destination.getCurrentVolume();
            double projectedDestination = destinationVolume + quantityKg;
            Double maxCapacity = destination.getMaxCapacity();
            if (maxCapacity != null && maxCapacity > 0 && projectedDestination > maxCapacity) {
                throw new IllegalStateException(String.format(
                        "Operation refusee: capacite depassee sur la cuve destination (%s). Actuel=%.3f, ajout=%.3f, max=%.3f",
                        destination.getName(), destinationVolume, quantityKg, maxCapacity));
            }
        }
    }

    private void validateReverseNonNegativeVolumeBeforeSave(StorageUnit destination, Double quantityKg) {
        if (destination == null || quantityKg == null || quantityKg <= 0) {
            return;
        }

        double destinationVolume = destination.getCurrentVolume() == null ? 0d : destination.getCurrentVolume();
        double projectedDestination = destinationVolume - quantityKg;
        if (projectedDestination < 0) {
            throw new IllegalStateException(String.format(
                    "Annulation refusee: volume negatif detecte sur la cuve destination (%s). Actuel=%.3f, a retirer=%.3f",
                    destination.getName(), destinationVolume, quantityKg));
        }
    }
}
