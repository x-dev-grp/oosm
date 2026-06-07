package com.xdev.ooms.production.repository;

import  com.xdev.ooms.sharedkernel.Enum.TransactionType;
import com.xdev.ooms.production.model.OilTransaction;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OilTransactionRepository extends BaseRepository<OilTransaction> {
    List<OilTransaction> findByStorageUnitDestinationId(UUID storageUnitId);
    // Add this method for payment validation
    List<OilTransaction> findByOilSaleId(UUID oilSaleId);
    Optional<OilTransaction> findByOilSaleIdAndIsDeletedFalse(UUID oilSaleId);
    Optional<OilTransaction> findFirstByOilSaleIdOrderByCreatedDateDesc(UUID oilSaleId);
    Optional<OilTransaction> findFirstByStorageUnitDestinationIdAndTransactionTypeOrderByCreatedDateAsc(UUID storageUnitId, TransactionType transactionType);

    List<OilTransaction> findAllByStorageUnitDestinationIdAndTransactionTypeAndIsDeletedFalseOrderByCreatedDateAsc(
            UUID storageUnitId, TransactionType transactionType);
}
